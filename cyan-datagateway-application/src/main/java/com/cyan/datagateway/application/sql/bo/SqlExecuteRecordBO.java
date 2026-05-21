package com.cyan.datagateway.application.sql.bo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * SQL执行记录BO
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Data
public class SqlExecuteRecordBO {

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
    private String sqlType;

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
    private String status;

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
     * 执行来源
     */
    private String source;
}
