package com.cyan.datagateway.application.sql.impl;

import com.cyan.datagateway.application.sql.SqlExecuteService;
import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd;
import com.cyan.datagateway.enums.SqlExecuteStatus;
import com.cyan.datagateway.infra.util.StarRocksUtil;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SqlExecuteServiceImpl implements SqlExecuteService {

    private final StarRocksUtil starRocksUtil;


    /**
     * 执行SQL查询
     *
     * @param cmd SQL执行命令
     * @return 执行结果
     */
    @Override
    public SqlExecuteResultBO execute(SqlExecuteCmd cmd) {
        long start = System.currentTimeMillis();
        try {
            StarRocksUtil.ExecuteResult result = starRocksUtil.execute(cmd.getSql());
            long end = System.currentTimeMillis();
            return new SqlExecuteResultBO()
                    .setExecuteId("1")
                    .setStatus(SqlExecuteStatus.SUCCESS)
                    .setCostTimeMs(end - start)
                    .setData(result.getResultList());
        } catch (Exception e) {
            return new SqlExecuteResultBO()
                    .setExecuteId("1")
                    .setStatus(SqlExecuteStatus.FAILED)
                    .setErrorMessage(e.getMessage())
                    .setCostTimeMs(System.currentTimeMillis() - start);
        }
    }
}
