package com.cyan.datagateway.infra.util;

import com.cyan.arch.common.util.Convert;
import com.cyan.datagateway.enums.SqlExecuteStatus;
import com.cyan.datagateway.enums.SqlType;
import com.cyan.datagateway.infra.config.StarRocksProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * StarRocks 连接工具类
 * 支持 DDL/DML/DQL 全类型 SQL 执行，DQL（SELECT）自动追加默认 LIMIT
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Slf4j
@Component
public class StarRocksUtil {

    private final StarRocksProperties properties;
    private final String defaultCatalog;
    // 默认 LIMIT 条数（从配置读取，兜底 1000）
    private final int defaultLimit;

    // 匹配 LIMIT 语句（支持跨行，忽略大小写）
    private static final Pattern LIMIT_PATTERN = Pattern.compile("\\s+LIMIT\\s+\\d+\\s*;?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 匹配所有空白字符（换行/制表符/多个空格）
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    // 匹配单行注释 -- 开头到行尾
    private static final Pattern SINGLE_LINE_COMMENT = Pattern.compile("--[^\\n]*");
    // 匹配多行注释 /* */
    private static final Pattern MULTI_LINE_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    // 查询结果封装
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
         * 最终执行的SQL语句（包含自动追加的LIMIT）
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

        // 快速创建失败结果的静态方法
        public static QueryResult fail(String sql, long costTimeMs, SqlExecuteStatus status,String msg) {
            return new QueryResult()
                    .setResultList(new ArrayList<>())
                    .setRowCount(0)
                    .setSql(sql)
                    .setCostTimeMs(costTimeMs)
                    .setMsg(msg)
                    .setSqlExecuteStatus(status);
        }
    }

    public StarRocksUtil(StarRocksProperties properties) {
        this.properties = properties;
        this.defaultCatalog = "iceberg";
        this.defaultLimit = 1000;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL JDBC Driver not found!");
        }
    }

    /**
     * 获取数据库连接，并自动设置会话默认 Catalog
     */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                properties.getJdbcUrl(),
                properties.getUsername(),
                properties.getPassword()
        );
        try (Statement stmt = conn.createStatement()) {
            String setCatalogSql = String.format("SET CATALOG %s;", defaultCatalog);
            stmt.execute(setCatalogSql);
        } catch (SQLException e) {
            log.error("设置 Catalog 失败，关闭无效连接: {}", e.getMessage());
            close(conn);
            throw new SQLException("设置默认 Catalog 失败: " + e.getMessage(), e);
        }
        return conn;
    }

    /**
     * 标准化 SQL 语句（移除注释、多余空白/换行，统一格式）
     */
    private String normalizeSql(String sql) {
        if (sql == null) {
            return null;
        }
        // 1. 移除单行注释 -- 开头到行尾
        String normalized = SINGLE_LINE_COMMENT.matcher(sql).replaceAll(" ");
        // 2. 移除多行注释 /* */
        normalized = MULTI_LINE_COMMENT.matcher(normalized).replaceAll(" ");
        // 3. 将所有空白字符（换行/制表符/多个空格）替换为单个空格
        normalized = WHITESPACE_PATTERN.matcher(normalized).replaceAll(" ");
        // 4. 首尾去空格
        normalized = normalized.trim();
        return normalized;
    }

    /**
     * 执行 SQL 语句（支持 DDL/DML/DQL 全类型）
     * - DQL（SELECT）：自动追加默认 LIMIT（如未指定）
     * - DML（INSERT/UPDATE/DELETE）：返回影响行数
     * - DDL（CREATE/ALTER/DROP）：返回执行结果
     *
     * @param sql    SQL 语句
     * @param params SQL 参数（可选）
     * @return 查询结果封装对象（包含最终执行的SQL）
     */
    public QueryResult executeQuery(String sql, Object... params) {
        long start = System.currentTimeMillis();
        String finalSql = null;

        try {
            // 1. 校验 SQL 非空
            if (sql == null || sql.trim().isEmpty()) {
                throw new IllegalArgumentException("SQL语句不能为空");
            }

            // 2. 标准化 SQL（移除换行/多余空格）
            String normalizedSql = normalizeSql(sql);
            log.debug("标准化后的 SQL: {}", normalizedSql);

            // 3. 解析 SQL 类型
            SqlType sqlType = SqlType.parseFromSql(normalizedSql);
            log.info("SQL 类型: {}", sqlType);

            // 4. 根据 SQL 类型执行不同逻辑
            finalSql = normalizedSql;
            List<Map<String, Object>> resultList;

            try (Connection conn = getConnection()) {
                if (sqlType == SqlType.SELECT) {
                    // DQL: 追加默认 LIMIT（如未指定）
                    finalSql = addDefaultLimitForSelect(normalizedSql);
                    if (params == null || params.length == 0) {
                        resultList = executeSelectWithoutParams(conn, finalSql);
                    } else {
                        resultList = executeSelectWithParams(conn, finalSql, params);
                    }
                } else if (sqlType == SqlType.INSERT || sqlType == SqlType.UPDATE || sqlType == SqlType.DELETE) {
                    // DML: 执行更新操作
                    int affectedRows = executeUpdate(conn, finalSql, params);
                    resultList = buildAffectedRowsResult(affectedRows);
                } else {
                    // DDL 及其他: 执行并返回结果
                    resultList = executeOther(conn, finalSql);
                }
            }

            // 5. 封装成功结果
            long costTime = System.currentTimeMillis() - start;
            return QueryResult.of(resultList, finalSql)
                    .setCostTimeMs(costTime)
                    .setSqlExecuteStatus(SqlExecuteStatus.SUCCESS);

        } catch (IllegalArgumentException e) {
            // 非法参数异常
            long costTime = System.currentTimeMillis() - start;
            log.error("StarRocks SQL 参数校验失败, sql={}, error={}",
                    finalSql == null ? sql : finalSql, e.getMessage(), e);
            return QueryResult.fail(finalSql == null ? sql : finalSql, costTime, SqlExecuteStatus.FAILED, e.getMessage());

        } catch (SQLException e) {
            // 数据库执行异常
            long costTime = System.currentTimeMillis() - start;
            log.error("StarRocks SQL 执行失败, sql={}, error={}",
                    finalSql == null ? sql : finalSql, e.getMessage(), e);
            return QueryResult.fail(finalSql == null ? sql : finalSql, costTime, SqlExecuteStatus.FAILED, e.getMessage());

        } catch (Exception e) {
            // 其他未知异常
            long costTime = System.currentTimeMillis() - start;
            log.error("StarRocks SQL 执行异常, sql={}, error={}",
                    finalSql == null ? sql : finalSql, e.getMessage(), e);
            return QueryResult.fail(finalSql == null ? sql : finalSql, costTime, SqlExecuteStatus.FAILED,e.getMessage());
        }
    }

    /**
     * 执行 DML 更新操作（INSERT/UPDATE/DELETE）
     */
    private int executeUpdate(Connection conn, String sql, Object[] params) throws SQLException {
        if (params == null || params.length == 0) {
            try (Statement stmt = conn.createStatement()) {
                return stmt.executeUpdate(sql);
            }
        } else {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
                return pstmt.executeUpdate();
            }
        }
    }

    /**
     * 执行 DDL 或其他语句
     */
    private List<Map<String, Object>> executeOther(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            boolean hasResultSet = stmt.execute(sql);
            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return resultSetToList(rs);
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                return buildAffectedRowsResult(updateCount);
            }
        }
    }

    /**
     * 构建影响行数结果
     */
    private List<Map<String, Object>> buildAffectedRowsResult(int affectedRows) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("affected_rows", affectedRows);
        result.add(row);
        return result;
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
        String sqlWithLimit = sqlWithoutSemicolon + " LIMIT " + defaultLimit;
        // 保持分号习惯
        if (trimSql.endsWith(";")) {
            sqlWithLimit += ";";
        }
        return sqlWithLimit;
    }

    /**
     * 执行不带参数的 SELECT 查询
     */
    private List<Map<String, Object>> executeSelectWithoutParams(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            long stmtStart = System.currentTimeMillis();
            try (ResultSet rs = stmt.executeQuery(sql)) {
                log.debug("SQL 执行耗时: {}ms", System.currentTimeMillis() - stmtStart);
                return resultSetToList(rs);
            }
        }
    }

    /**
     * 执行带参数的 SELECT 查询
     */
    private List<Map<String, Object>> executeSelectWithParams(Connection conn, String sql, Object[] params) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            long stmtStart = System.currentTimeMillis();
            // 执行查询
            try (ResultSet rs = pstmt.executeQuery()) {
                log.debug("SQL 执行耗时: {}ms", System.currentTimeMillis() - stmtStart);
                return resultSetToList(rs);
            }
        }
    }

    /**
     * 将 ResultSet 转换为 List<Map<String, Object>>
     */
    private List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Object value = rs.getObject(i);
                // 处理 LocalDateTime 类型转换
                if (value instanceof LocalDateTime) {
                    value = Convert.toDateTimeStr(value);
                }
                row.put(columnName, value);
            }
            result.add(row);
        }
        log.debug("ResultSet 转换完成，共 {} 行数据", result.size());
        return result;
    }

    // --------------------- 资源关闭方法 ---------------------
    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Failed to close ResultSet: {}", e.getMessage());
            }
        }
    }

    public static void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error("Failed to close Statement: {}", e.getMessage());
            }
        }
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Failed to close Connection: {}", e.getMessage());
            }
        }
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        close(rs);
        close(stmt);
        close(conn);
    }
}
