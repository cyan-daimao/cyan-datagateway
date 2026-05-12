package com.cyan.datagateway.application.sql.cmd;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * SQL执行命令
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Data
public class SqlExecuteCmd {

    @NotBlank(message = "SQL内容不能为空")
    private String sql;


    private Long timeoutMs = 60000L;

    /**
     * 执行来源：SQL / METRIC / SPARK
     */
    private String source;
}
