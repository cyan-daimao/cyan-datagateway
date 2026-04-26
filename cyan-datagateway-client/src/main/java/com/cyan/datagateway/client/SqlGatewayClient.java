package com.cyan.datagateway.client;

import com.cyan.datagateway.client.cmd.SqlExecuteCmd;
import com.cyan.datagateway.client.dto.SqlExecuteResultDTO;
import com.cyan.arch.common.api.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * SQL网关RPC客户端
 *
 * @author cy.Y
 * @since 1.0.0
 */
@FeignClient(name = "SqlGatewayClient", path = "/rpc/v1/datagateway")
public interface SqlGatewayClient {

    /**
     * 执行StarRocks SQL
     *
     * @param cmd SQL执行命令
     * @return 执行结果
     */
    @PostMapping("/starrocks/execute")
    Response<SqlExecuteResultDTO> executeStarRocksSql(@RequestBody SqlExecuteCmd cmd);

    /**
     * 执行Spark SQL
     *
     * @param cmd SQL执行命令
     * @return 执行结果
     */
    @PostMapping("/spark/execute")
    Response<SqlExecuteResultDTO> executeSparkSql(@RequestBody SqlExecuteCmd cmd);
}
