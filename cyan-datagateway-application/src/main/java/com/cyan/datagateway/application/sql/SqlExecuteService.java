package com.cyan.datagateway.application.sql;

import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd;

/**
 * SQL执行服务接口
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
public interface SqlExecuteService {

    /**
     * 执行SQL查询
     *
     * @param cmd      SQL执行命令
     * @return 执行结果
     */
    SqlExecuteResultBO execute(SqlExecuteCmd cmd);

}
