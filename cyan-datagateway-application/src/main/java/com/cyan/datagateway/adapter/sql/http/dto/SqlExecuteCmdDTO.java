package com.cyan.datagateway.adapter.sql.http.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * SQL执行命令DTO（用于更新操作）
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Data
public class SqlExecuteCmdDTO {

    @NotBlank(message = "SQL内容不能为空")
    private String sql;

    private String database;

    private Long timeoutMs = 60000L;
}
