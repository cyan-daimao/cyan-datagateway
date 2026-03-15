package com.cyan.datagateway.adapter.sql.http.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * SQL执行请求DTO
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Data
public class SqlExecuteDTO {

    @NotBlank(message = "SQL内容不能为空")
    private String sql;

    private String database;

    private Long timeoutMs = 60000L;
}
