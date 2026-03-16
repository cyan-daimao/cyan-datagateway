package com.cyan.datagateway.infra.util;

import com.cyan.arch.common.util.Convert;
import com.cyan.datagateway.infra.config.StarRocksProperties;
import lombok.Data;
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
 * 仅支持 SELECT 查询语句，并自动给 SELECT 加默认 LIMIT
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
    // 匹配 SELECT 语句的正则（忽略大小写，匹配是否已包含 LIMIT）
    private static final Pattern SELECT_PATTERN = Pattern.compile("^\\s*SELECT.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern LIMIT_PATTERN = Pattern.compile("\\s+LIMIT\\s+\\d+\\s*$", Pattern.CASE_INSENSITIVE);

    // 查询结果封装
    @Data
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

        public static QueryResult of(List<Map<String, Object>> resultList) {
            QueryResult result = new QueryResult();
            result.resultList = resultList;
            result.rowCount = resultList.size();
            return result;
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
     * 执行 SELECT 查询（自动追加默认 LIMIT）
     *
     * @param sql    SELECT 查询语句
     * @param params SQL 参数（可选）
     * @return 查询结果封装对象
     * @throws SQLException             数据库执行异常
     * @throws IllegalArgumentException 非 SELECT 语句时抛出
     */
    public QueryResult executeQuery(String sql, Object... params) throws SQLException {
        long start = System.currentTimeMillis();
        // 1. 校验 SQL 非空
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }

        // 2. 强制校验必须是 SELECT 语句
        String trimSql = sql.trim();
        if (!SELECT_PATTERN.matcher(trimSql).matches()) {
            throw new IllegalArgumentException("该工具类仅支持 SELECT 查询语句，不支持 DML/DDL 操作");
        }

        // 3. 追加默认 LIMIT
        String finalSql = addDefaultLimitForSelect(sql);
        // 4. 执行查询
        try (Connection conn = getConnection()) {
            List<Map<String, Object>> resultList;
            if (params == null || params.length == 0) {
                resultList = executeSelectWithoutParams(conn, finalSql);
            } else {
                resultList = executeSelectWithParams(conn, finalSql, params);
            }
            QueryResult queryResult = QueryResult.of(resultList);
            queryResult.setCostTimeMs(System.currentTimeMillis()-start);
            return queryResult;
        } catch (SQLException e) {
            log.error("SELECT 查询失败: {}，原始 SQL: {}，最终 SQL: {}", e.getMessage(), sql, finalSql);
            throw e;
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

        // 判断是否已包含 LIMIT
        if (LIMIT_PATTERN.matcher(sqlWithoutSemicolon).find()) {
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