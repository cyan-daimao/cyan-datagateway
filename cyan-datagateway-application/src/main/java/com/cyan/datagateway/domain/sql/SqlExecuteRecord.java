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

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 执行ID
     */
    private String executeId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * SQL内容
     */
    private String sqlContent;

    /**
     * SQL类型
     */
    private SqlType sqlType;

    /**
     * 查询引擎
     */
    private String queryEngine;

    /**
     * 目标数据库
     */
    private String database;

    /**
     * 执行状态
     */
    private SqlExecuteStatus status;

    /**
     * 影响行数/返回行数
     */
    private Long rowCount;

    /**
     * 执行耗时(毫秒)
     */
    private Long costTimeMs;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
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
