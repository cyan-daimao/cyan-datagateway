package com.cyan.datagateway.client.cmd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 执行sql的命令
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SqlExecuteCmd {

    /**
     * SQL内容
     */
    private String sql;

    /**
     * 执行人
     */
    private String passport;
}
