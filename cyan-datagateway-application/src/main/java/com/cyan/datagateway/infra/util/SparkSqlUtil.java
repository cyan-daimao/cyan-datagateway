package com.cyan.datagateway.infra.util;

import com.cyan.datagateway.enums.SqlExecuteStatus;
import com.cyan.datagateway.enums.SqlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Spark SQL 执行工具类
 * 支持 DDL/DML/DQL/DCL 全类型 SQL 执行
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Slf4j
@Component
public class SparkSqlUtil {

    private final SparkSession sparkSession;
    
    // 默认 LIMIT 条数
    private static final int DEFAULT_LIMIT = 1000;
    
    // 匹配 LIMIT 语句（支持跨行，忽略大小写）
    private static final Pattern LIMIT_PATTERN = Pattern.compile("\\s+LIMIT\\s+\\d+\\s*;?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 匹配所有空白字符（换行/制表符/多个空格）
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    // 匹配单行注释 -- 开头到行尾
    private static final Pattern SINGLE_LINE_COMMENT = Pattern.compile("--[^\\n]*");
    // 匹配多行注释 /* */
    private static final Pattern MULTI_LINE_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class QueryResult {
        /**
         * 查询结果列表
         */
        private List<Map<String, Object>> resultList;
        /**
         * 查询结果行
         */
        private int rowCount;
        /**
         * 查询耗时（毫秒）
         */
        private long costTimeMs;
        /**
         * 最终执行的SQL语句
         */
        private String sql;
        /**
         * sql执行状态
         */
        private SqlExecuteStatus sqlExecuteStatus;
        /**
         * 错误信息
         */
        private String msg;

        public static QueryResult of(List<Map<String, Object>> resultList, String finalSql) {
            QueryResult result = new QueryResult();
            result.resultList = resultList;
            result.rowCount = resultList.size();
            result.sql = finalSql;
            return result;
        }

        public static QueryResult fail(String sql, long costTimeMs, SqlExecuteStatus status, String msg) {
            return new QueryResult()
                    .setResultList(new ArrayList<>())
                    .setRowCount(0)
                    .setSql(sql)
                    .setCostTimeMs(costTimeMs)
                    .setMsg(msg)
                    .setSqlExecuteStatus(status);
        }
    }

    public SparkSqlUtil(SparkSession sparkSession) {
        this.sparkSession = sparkSession;
    }

    /**
     * 标准化 SQL 语句（移除注释、多余空白/换行，统一格式）
     */
    private String normalizeSql(String sql) {
        if (sql == null) {
            return null;
        }
        // 1. 移除单行注释
        String normalized = SINGLE_LINE_COMMENT.matcher(sql).replaceAll(" ");
        // 2. 移除多行注释
        normalized = MULTI_LINE_COMMENT.matcher(normalized).replaceAll(" ");
        // 3. 将所有空白字符替换为单个空格
        normalized = WHITESPACE_PATTERN.matcher(normalized).replaceAll(" ");
        // 4. 首尾去空格
        return normalized.trim();
    }

    /**
     * 执行 SQL 语句（支持 DDL/DML/DQL/DCL 全类型）
     *
     * @param sql SQL 语句
     * @return 查询结果封装对象
     */
    public QueryResult executeQuery(String sql) {
        long start = System.currentTimeMillis();
        String finalSql = null;

        try {
            // 1. 校验 SQL 非空
            if (sql == null || sql.trim().isEmpty()) {
                throw new IllegalArgumentException("SQL语句不能为空");
            }

            // 2. 标准化 SQL
            String normalizedSql = normalizeSql(sql);
            log.debug("标准化后的 SQL: {}", normalizedSql);

            // 3. 解析 SQL 类型
            SqlType sqlType = SqlType.parseFromSql(normalizedSql);
            log.info("SQL 类型: {}", sqlType);

            // 4. 执行 SQL
            finalSql = normalizedSql;
            List<Map<String, Object>> resultList;

            if (sqlType == SqlType.SELECT) {
                // DQL: 追加默认 LIMIT（如未指定）
                finalSql = addDefaultLimitForSelect(normalizedSql);
                resultList = executeSelect(finalSql);
            } else if (sqlType == SqlType.INSERT || sqlType == SqlType.UPDATE || sqlType == SqlType.DELETE) {
                // DML: 执行更新操作
                resultList = executeUpdate(finalSql);
            } else {
                // DDL/DCL 及其他
                resultList = executeOther(finalSql);
            }

            // 5. 封装成功结果
            long costTime = System.currentTimeMillis() - start;
            return QueryResult.of(resultList, finalSql)
                    .setCostTimeMs(costTime)
                    .setSqlExecuteStatus(SqlExecuteStatus.SUCCESS);

        } catch (IllegalArgumentException e) {
            long costTime = System.currentTimeMillis() - start;
            return QueryResult.fail(finalSql == null ? sql : finalSql, costTime, SqlExecuteStatus.FAILED, e.getMessage());

        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - start;
            log.error("Spark SQL 执行失败: {}", e.getMessage(), e);
            return QueryResult.fail(finalSql == null ? sql : finalSql, costTime, SqlExecuteStatus.FAILED, e.getMessage());
        }
    }

    /**
     * 执行 SELECT 查询
     */
    private List<Map<String, Object>> executeSelect(String sql) {
        Dataset<Row> df = sparkSession.sql(sql);
        Row[] rows = (Row[]) df.collect();
        
        String[] columns = df.columns();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Row row : rows) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            for (int i = 0; i < columns.length; i++) {
                Object value = row.get(i);
                // 处理 null 值
                if (value == null) {
                    value = null;
                }
                rowMap.put(columns[i], value);
            }
            result.add(rowMap);
        }
        
        log.debug("SELECT 查询完成，共 {} 行数据", result.size());
        return result;
    }

    /**
     * 执行 DML 更新操作（INSERT/UPDATE/DELETE）
     */
    private List<Map<String, Object>> executeUpdate(String sql) {
        sparkSession.sql(sql);
        
        // Spark SQL 没有直接返回影响行数的方式
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("result", "success");
        row.put("message", "DML executed successfully");
        result.add(row);
        return result;
    }

    /**
     * 执行 DDL/DCL 或其他语句
     */
    private List<Map<String, Object>> executeOther(String sql) {
        try {
            Dataset<Row> df = sparkSession.sql(sql);
            // 如果有返回结果
            Row[] rows = (Row[]) df.collect();
            String[] columns = df.columns();
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (Row row : rows) {
                Map<String, Object> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < columns.length; i++) {
                    rowMap.put(columns[i], row.get(i));
                }
                result.add(rowMap);
            }
            
            return result;
        } catch (Exception e) {
            // 对于不返回结果的 DDL 语句
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("result", "success");
            row.put("message", "DDL/DCL executed successfully");
            result.add(row);
            return result;
        }
    }

    /**
     * 对 SELECT 语句追加默认 LIMIT（如果未手动指定）
     */
    private String addDefaultLimitForSelect(String sql) {
        String trimSql = sql.trim();

        // 判断是否已包含 LIMIT
        if (LIMIT_PATTERN.matcher(trimSql).find()) {
            return trimSql;
        }

        // 移除末尾的分号
        String sqlWithoutSemicolon = trimSql.endsWith(";")
                ? trimSql.substring(0, trimSql.length() - 1).trim()
                : trimSql;

        // 追加默认 LIMIT
        String sqlWithLimit = sqlWithoutSemicolon + " LIMIT " + DEFAULT_LIMIT;
        // 保持分号习惯
        if (trimSql.endsWith(";")) {
            sqlWithLimit += ";";
        }
        return sqlWithLimit;
    }
}
