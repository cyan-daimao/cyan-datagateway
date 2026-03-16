package com.cyan.datagateway.infra.util;

import com.cyan.arch.common.util.Convert;
import com.cyan.datagateway.infra.config.StarRocksProperties;
import lombok.Getter;
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
 * 支持自动给 SELECT 加默认 LIMIT，仅适配 SELECT 语句，不影响其他 DML/DDL
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

    // 通用执行结果封装
    @Getter
    public static class ExecuteResult {
        // getter & setter
        private boolean hasResultSet;
        private List<Map<String, Object>> resultList;
        private int affectedRows;
        private boolean executeSuccess;

        public static ExecuteResult ofResultSet(List<Map<String, Object>> resultList) {
            ExecuteResult result = new ExecuteResult();
            result.hasResultSet = true;
            result.resultList = resultList;
            return result;
        }

        public static ExecuteResult ofAffectedRows(int affectedRows) {
            ExecuteResult result = new ExecuteResult();
            result.hasResultSet = false;
            result.affectedRows = affectedRows;
            return result;
        }

        public static ExecuteResult ofDdlSuccess(boolean executeSuccess) {
            ExecuteResult result = new ExecuteResult();
            result.hasResultSet = false;
            result.executeSuccess = executeSuccess;
            return result;
        }

    }

    public StarRocksUtil(StarRocksProperties properties) {
        this.properties = properties;
        this.defaultCatalog = "iceberg";
        // 从配置读取默认 LIMIT 条数，兜底 1000
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
            log.debug("成功设置会话默认 Catalog: {}", defaultCatalog);
        } catch (SQLException e) {
            log.error("设置 Catalog 失败，关闭无效连接: {}", e.getMessage());
            close(conn);
            throw new SQLException("设置默认 Catalog 失败: " + e.getMessage(), e);
        }
        return conn;
    }

    /**
     * 通用 SQL 执行方法（自动给 SELECT 加默认 LIMIT，其他语句不处理）
     * @param sql 要执行的 SQL 语句
     * @param params SQL 参数（可选）
     * @return 执行结果封装对象
     * @throws SQLException 数据库执行异常
     */
    public ExecuteResult execute(String sql, Object... params) throws SQLException {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }

        // 核心逻辑：仅对 SELECT 语句追加默认 LIMIT（未手动指定 LIMIT 时）
        String finalSql = addDefaultLimitForSelect(sql);
        log.info("最终执行 SQL: {}", finalSql);
        if (params != null && params.length > 0) {
            log.debug("SQL 参数: {}", params);
        }

        try (Connection conn = getConnection()) {
            if (params == null || params.length == 0) {
                return executeWithoutParams(conn, finalSql);
            } else {
                return executeWithParams(conn, finalSql, params);
            }
        } catch (SQLException e) {
            log.error("SQL 执行失败: {}，原始 SQL: {}，最终 SQL: {}", e.getMessage(), sql, finalSql);
            throw e;
        }
    }

    /**
     * 仅对 SELECT 语句追加默认 LIMIT（如果未手动指定）
     * 修复：先移除末尾分号，再追加 LIMIT，避免语法错误
     */
    private String addDefaultLimitForSelect(String sql) {
        String trimSql = sql.trim();
        // 1. 判断是否是 SELECT 语句
        if (!SELECT_PATTERN.matcher(trimSql).matches()) {
            return sql; // 非 SELECT 语句，直接返回
        }

        // 2. 移除末尾的分号（关键修复点）
        String sqlWithoutSemicolon = trimSql.endsWith(";")
                ? trimSql.substring(0, trimSql.length() - 1).trim()
                : trimSql;

        // 3. 判断是否已包含 LIMIT（基于移除分号后的 SQL）
        if (LIMIT_PATTERN.matcher(sqlWithoutSemicolon).find()) {
            log.debug("SELECT 语句已包含 LIMIT，无需追加");
            return trimSql; // 保留用户原始的分号格式
        }

        // 4. 追加默认 LIMIT（移除分号后追加，最后可选择性加回分号）
        String sqlWithLimit = sqlWithoutSemicolon + " LIMIT " + defaultLimit;
        // 保持用户原始的分号习惯：如果用户原来有分号，最后加回
        if (trimSql.endsWith(";")) {
            sqlWithLimit += ";";
        }
        log.debug("为 SELECT 语句追加默认 LIMIT {}，原始 SQL: {}，最终 SQL: {}",
                defaultLimit, sql, sqlWithLimit);
        return sqlWithLimit;
    }

    /**
     * 执行不带参数的 SQL
     */
    private ExecuteResult executeWithoutParams(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            boolean hasResultSet = stmt.execute(sql);
            return parseExecuteResult(stmt, hasResultSet);
        }
    }

    /**
     * 执行带参数的 SQL
     */
    private ExecuteResult executeWithParams(Connection conn, String sql, Object[] params) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            boolean hasResultSet = pstmt.execute();
            return parseExecuteResult(pstmt, hasResultSet);
        }
    }

    /**
     * 解析执行结果
     */
    private ExecuteResult parseExecuteResult(Statement stmt, boolean hasResultSet) throws SQLException {
        if (hasResultSet) {
            try (ResultSet rs = stmt.getResultSet()) {
                List<Map<String, Object>> resultList = resultSetToList(rs);
                log.info("SQL 执行成功，返回 {} 条结果", resultList.size());
                return ExecuteResult.ofResultSet(resultList);
            }
        } else {
            int affectedRows = stmt.getUpdateCount();
            if (affectedRows != -1) {
                log.info("SQL 执行成功，影响行数: {}", affectedRows);
                return ExecuteResult.ofAffectedRows(affectedRows);
            } else {
                log.info("DDL 语句执行成功");
                return ExecuteResult.ofDdlSuccess(true);
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
                if (value instanceof LocalDateTime) {
                    value = Convert.toDateTimeStr(value);
                }
                row.put(columnName, value);
            }
            result.add(row);
        }
        return result;
    }

    // --------------------- 保留原有兼容方法 ---------------------
    public List<Map<String, Object>> queryForList(String sql) throws SQLException {
        ExecuteResult result = execute(sql);
        return result.isHasResultSet() ? result.getResultList() : new ArrayList<>();
    }

    public List<Map<String, Object>> queryForList(String sql, Object... params) throws SQLException {
        ExecuteResult result = execute(sql, params);
        return result.isHasResultSet() ? result.getResultList() : new ArrayList<>();
    }

    public int executeUpdate(String sql) throws SQLException {
        ExecuteResult result = execute(sql);
        return result.isHasResultSet() ? 0 : result.getAffectedRows();
    }

    public int executeUpdate(String sql, Object... params) throws SQLException {
        ExecuteResult result = execute(sql, params);
        return result.isHasResultSet() ? 0 : result.getAffectedRows();
    }

    public boolean executeDdl(String sql) throws SQLException {
        ExecuteResult result = execute(sql);
        return result.isHasResultSet() ? false : result.isExecuteSuccess();
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