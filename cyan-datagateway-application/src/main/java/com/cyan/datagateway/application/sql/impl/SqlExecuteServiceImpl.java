package com.cyan.datagateway.application.sql.impl;

import com.cyan.datagateway.application.sql.SqlExecuteService;
import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd;
import com.cyan.datagateway.infra.util.StarRocksUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SQL执行服务实现
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Slf4j
@Service
public class SqlExecuteServiceImpl implements SqlExecuteService {

    private final StarRocksUtil starRocksUtil;

    public SqlExecuteServiceImpl(StarRocksUtil starRocksUtil) {
        this.starRocksUtil = starRocksUtil;
    }


    /**
     * 执行SQL查询
     *
     * @param cmd SQL执行命令
     * @return 执行结果
     */
    @Override
    public SqlExecuteResultBO execute(SqlExecuteCmd cmd) {
        StarRocksUtil.QueryResult result = starRocksUtil.executeQuery(cmd.getSql());

        return new SqlExecuteResultBO()
                .setExecuteId("1")
                .setStatus(result.getSqlExecuteStatus())
                .setCostTimeMs(result.getCostTimeMs())
                .setData(result.getResultList())
                .setErrorMessage(result.getMsg())
                .setSql(result.getSql());
    }
}
