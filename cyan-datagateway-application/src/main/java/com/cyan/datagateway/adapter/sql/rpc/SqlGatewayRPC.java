package com.cyan.datagateway.adapter.sql.rpc;

import com.cyan.arch.common.api.Response;
import com.cyan.datagateway.adapter.sql.rpc.convert.SqlGatewayConvert;
import com.cyan.datagateway.application.sql.SparkSqlService;
import com.cyan.datagateway.application.sql.SqlExecuteService;
import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.client.SqlGatewayClient;
import com.cyan.datagateway.client.cmd.SqlExecuteCmd;
import com.cyan.datagateway.client.dto.SqlExecuteResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SQL网关RPC实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/rpc/v1/datagateway")
public class SqlGatewayRPC implements SqlGatewayClient {

    private final SqlExecuteService sqlExecuteService;
    private final SparkSqlService sparkSqlService;

    @Override
    public Response<SqlExecuteResultDTO> executeStarRocksSql(SqlExecuteCmd cmd) {
        com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd appCmd =
                new com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd();
        appCmd.setSql(cmd.getSql());
        appCmd.setSource("SQL");
        SqlExecuteResultBO resultBO = sqlExecuteService.execute(appCmd);
        SqlExecuteResultDTO resultDTO = SqlGatewayConvert.INSTANCE.toSqlExecuteResultDTO(resultBO);
        return Response.success(resultDTO);
    }

    @Override
    public Response<SqlExecuteResultDTO> executeMetricSql(SqlExecuteCmd cmd) {
        com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd appCmd =
                new com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd();
        appCmd.setSql(cmd.getSql());
        appCmd.setSource("METRIC");
        SqlExecuteResultBO resultBO = sqlExecuteService.execute(appCmd);
        SqlExecuteResultDTO resultDTO = SqlGatewayConvert.INSTANCE.toSqlExecuteResultDTO(resultBO);
        return Response.success(resultDTO);
    }

    @Override
    public Response<SqlExecuteResultDTO> executeSparkSql(SqlExecuteCmd cmd) {
        com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd appCmd =
                new com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd();
        appCmd.setSql(cmd.getSql());
        appCmd.setSource("SPARK");
        SqlExecuteResultBO resultBO = sparkSqlService.execute(appCmd);
        SqlExecuteResultDTO resultDTO = SqlGatewayConvert.INSTANCE.toSqlExecuteResultDTO(resultBO);
        return Response.success(resultDTO);
    }
}
