package com.cyan.datagateway.adapter.sql.http.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * SQL执行记录查询DTO
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Data
public class SqlExecuteRecordQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private Long userId;

    private String userName;

    private String sqlType;

    private String status;

    private String executeId;

    private LocalDateTime startTimeBegin;

    private LocalDateTime startTimeEnd;
}
