package com.cyan.datagateway.infra.util;

import com.cyan.arch.common.util.Convert;
import com.cyan.datagateway.enums.SqlExecuteStatus;
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
 * 仅支持 SELECT 查询语句（含 EXPLAIN SELECT），并自动给 SELECT 加默认 LIMIT
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

    // 匹配 SELECT 语句的正则（支持 EXPLAIN/DESC 前缀，忽略大小写）
    // 匹配规则：开头可选的 EXPLAIN/DESC + 任意空白 + SELECT 开头的语句
    private static final Pattern SELECT_PATTERN = Pattern.compile("^\\s*(EXPLAIN|DESC|DESCRIBE)?\\s*SELECT.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern LIMIT_PATTERN = Pattern.compile("\\s+LIMIT\\s+\\d+\\s*$", Pattern.CASE_INSENSITIVE);

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

        public static QueryResult of(List<Map<String, Object>> resultList, String finalSql) {
            QueryResult result = new QueryResult();
            result.resultList = resultList;
            result.rowCount = resultList.size();
            result.sql = finalSql; // 赋值最终执行的SQL
            return result;
        }

        // 快速创建失败结果的静态方法
        public static QueryResult fail(String sql, long costTimeMs, SqlExecuteStatus status) {
            return new QueryResult()
                    .setResultList(new ArrayList<>())
                    .setRowCount(0)
                    .setSql(sql)
                    .setCostTimeMs(costTimeMs)
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
     * 执行 SELECT 查询（含 EXPLAIN SELECT，自动追加默认 LIMIT）
     *
     * @param sql    SELECT 查询语句（支持 EXPLAIN/DESC 前缀）
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

            // 2. 校验必须是 SELECT 相关语句（含 EXPLAIN/DESC 前缀）
            String trimSql = sql.trim();
            if (!SELECT_PATTERN.matcher(trimSql).matches()) {
                throw new IllegalArgumentException("该工具类仅支持 SELECT 查询语句（含 EXPLAIN/DESC SELECT），不支持 DML/DDL 操作");
            }

            // 3. 追加默认 LIMIT，得到最终执行的SQL
            finalSql = addDefaultLimitForSelect(sql);
            log.info("最终执行 SQL: {}", finalSql);
            if (params != null && params.length > 0) {
                log.debug("SQL 参数: {}", params);
            }

            // 4. 执行查询
            List<Map<String, Object>> resultList;
            try (Connection conn = getConnection()) {
                if (params == null || params.length == 0) {
                    resultList = executeSelectWithoutParams(conn, finalSql);
                } else {
                    resultList = executeSelectWithParams(conn, finalSql, params);
                }
            }

            // 5. 封装成功结果
            long costTime = System.currentTimeMillis() - start;
            return QueryResult.of(resultList, finalSql)
                    .setCostTimeMs(costTime)
                    .setSqlExecuteStatus(SqlExecuteStatus.SUCCESS);

        } catch (IllegalArgumentException e) {
            // 非法参数异常（如非SELECT语句、空SQL）
            long costTime = System.currentTimeMillis() - start;
            log.error("SQL 参数校验失败: {}，原始 SQL: {}", e.getMessage(), sql);
            return QueryResult.fail(finalSql == null ? sql : finalSql, costTime, SqlExecuteStatus.FAILED);

        } catch (SQLException e) {
            // 数据库执行异常
            long costTime = System.currentTimeMillis() - start;
            log.error("SELECT 查询失败: {}，原始 SQL: {}，最终 SQL: {}", e.getMessage(), sql, finalSql);
            return QueryResult.fail(finalSql == null ? sql : finalSql, costTime, SqlExecuteStatus.FAILED);

        } catch (Exception e) {
            // 其他未知异常
            long costTime = System.currentTimeMillis() - start;
            log.error("SQL 执行发生未知异常: {}，原始 SQL: {}", e.getMessage(), sql, e);
            return QueryResult.fail(finalSql == null ? sql : finalSql, costTime, SqlExecuteStatus.FAILED);
        }
    }

    /**
     * 仅对 SELECT 语句追加默认 LIMIT（如果未手动指定）
     * 先移除末尾分号，再追加 LIMIT，避免语法错误
     */
    private String addDefaultLimitForSelect(String sql) {
        String trimSql = sql.trim();

        // 移除末尾的分号
        String sqlWithoutSemicolon = trimSql.endsWith(";")
                ? trimSql.substring(0, trimSql.length() - 1).trim()
                : trimSql;

        // 修复：正确实现忽略大小写移除 EXPLAIN/DESC/DESCRIBE 前缀
        // 1. 编译带忽略大小写标志的正则表达式
        Pattern prefixPattern = Pattern.compile("^\\s*(EXPLAIN|DESC|DESCRIBE)\\s*", Pattern.CASE_INSENSITIVE);
        // 2. 匹配并替换前缀为空字符串
        String selectPart = prefixPattern.matcher(sqlWithoutSemicolon).replaceFirst("");

        // 判断是否已包含 LIMIT（基于移除前缀后的 SELECT 部分）
        if (LIMIT_PATTERN.matcher(selectPart).find()) {
            return trimSql; // 保留用户原始的分号格式
        }

        // 追加默认 LIMIT
        String sqlWithLimit = sqlWithoutSemicolon + " LIMIT " + defaultLimit;
        // 保持用户原始的分号习惯
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
            try (ResultSet rs = stmt.executeQuery(sql)) {
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
            // 执行查询
            try (ResultSet rs = pstmt.executeQuery()) {
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