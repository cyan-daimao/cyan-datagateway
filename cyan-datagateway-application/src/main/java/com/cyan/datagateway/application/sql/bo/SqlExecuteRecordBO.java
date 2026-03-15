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

    private Long id;

    private String executeId;

    private Long userId;

    private String userName;

    private String sqlContent;

    private String sqlType;

    private String queryEngine;

    private String database;

    private String status;

    private Long rowCount;

    private Long costTimeMs;

    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createdAt;
}
