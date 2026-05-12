package com.cyan.datagateway.domain.sql;

import com.cyan.datagateway.enums.SqlExecuteStatus;
import com.cyan.datagateway.enums.SqlType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SQL执行记录领域实体
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Data
public class SqlExecuteRecord {

    private Long id;

    private String executeId;

    private Long userId;

    private String userName;

    private String sqlContent;

    private SqlType sqlType;

    private String queryEngine;

    private String database;

    private SqlExecuteStatus status;

    private Long rowCount;

    private Long costTimeMs;

    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 执行来源
     */
    private String source;

    /**
     * 开始执行
     */
    public static SqlExecuteRecord create(String executeId, Long userId, String userName,
                                          String sqlContent, String queryEngine, String database,
                                          String source) {
        SqlExecuteRecord record = new SqlExecuteRecord();
        record.setExecuteId(executeId);
        record.setUserId(userId);
        record.setUserName(userName);
        record.setSqlContent(sqlContent);
        record.setSqlType(SqlType.parseFromSql(sqlContent));
        record.setQueryEngine(queryEngine);
        record.setDatabase(database);
        record.setSource(source);
        record.setStatus(SqlExecuteStatus.RUNNING);
        record.setStartTime(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        return record;
    }

    /**
     * 执行成功
     */
    public void success(Long rowCount, Long costTimeMs) {
        this.status = SqlExecuteStatus.SUCCESS;
        this.rowCount = rowCount;
        this.costTimeMs = costTimeMs;
        this.endTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 执行失败
     */
    public void fail(String errorMessage, Long costTimeMs) {
        this.status = SqlExecuteStatus.FAILED;
        this.errorMessage = errorMessage;
        this.costTimeMs = costTimeMs;
        this.endTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 执行超时
     */
    public void timeout(Long costTimeMs) {
        this.status = SqlExecuteStatus.TIMEOUT;
        this.costTimeMs = costTimeMs;
        this.endTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
