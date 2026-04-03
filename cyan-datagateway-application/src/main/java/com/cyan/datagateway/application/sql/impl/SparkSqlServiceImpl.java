package com.cyan.datagateway.application.sql.impl;

import com.cyan.datagateway.application.sql.SparkSqlService;
import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd;
import com.cyan.datagateway.infra.util.SparkSqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Spark SQL执行服务实现
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Slf4j
@Service
public class SparkSqlServiceImpl implements SparkSqlService {

    private final SparkSqlUtil sparkSqlUtil;

    public SparkSqlServiceImpl(SparkSqlUtil sparkSqlUtil) {
        this.sparkSqlUtil = sparkSqlUtil;
    }

    /**
     * 执行Spark SQL
     *
     * @param cmd SQL执行命令
     * @return 执行结果
     */
    @Override
    public SqlExecuteResultBO execute(SqlExecuteCmd cmd) {
        SparkSqlUtil.QueryResult result = sparkSqlUtil.executeQuery(cmd.getSql());

        return new SqlExecuteResultBO()
                .setExecuteId("spark-" + System.currentTimeMillis())
                .setStatus(result.getSqlExecuteStatus())
                .setCostTimeMs(result.getCostTimeMs())
                .setData(result.getResultList())
                .setErrorMessage(result.getMsg())
                .setSql(result.getSql());
    }
}
