package com.cyan.datagateway.application.sql;

import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd;

/**
 * Spark SQL执行服务接口
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
public interface SparkSqlService {

    /**
     * 执行Spark SQL
     *
     * @param cmd SQL执行命令
     * @return 执行结果
     */
    SqlExecuteResultBO execute(SqlExecuteCmd cmd);

}
