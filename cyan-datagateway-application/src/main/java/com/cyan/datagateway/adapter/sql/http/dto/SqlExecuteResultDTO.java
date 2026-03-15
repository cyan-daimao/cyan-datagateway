package com.cyan.datagateway.adapter.sql.http.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * SQL执行结果DTO
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Data
public class SqlExecuteResultDTO {

    private String executeId;

    private String status;

    private Long rowCount;

    private Long costTimeMs;

    private List<Map<String, Object>> data;

    private String errorMessage;
}
