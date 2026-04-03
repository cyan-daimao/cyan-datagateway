package com.cyan.datagateway.adapter.sql.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datagateway.adapter.sql.http.convert.SqlExecuteAdapterConvert;
import com.cyan.datagateway.adapter.sql.http.dto.SqlExecuteResultDTO;
import com.cyan.datagateway.application.sql.SparkSqlService;
import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spark SQL执行控制器
 * 支持 DDL/DML/DQL/DCL 全类型 SQL 执行
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@RestController
@RequestMapping("/api/v1/spark/sql")
@RequiredArgsConstructor
public class SparkSqlController {

    private final SparkSqlService sparkSqlService;

    /**
     * 执行 Spark SQL
     * 支持 DDL（CREATE/ALTER/DROP）、DML（INSERT/UPDATE/DELETE）、DQL（SELECT）、DCL（GRANT/REVOKE）等全类型 SQL
     *
     * @param cmd SQL执行命令
     * @return 执行结果
     */
    @PostMapping("/execute")
    public Response<SqlExecuteResultDTO> execute(@Valid @RequestBody SqlExecuteCmd cmd) {
        SqlExecuteResultBO resultBO = sparkSqlService.execute(cmd);
        SqlExecuteResultDTO resultDTO = SqlExecuteAdapterConvert.INSTANCE.toSqlExecuteResultDTO(resultBO);
        return Response.success(resultDTO);
    }

}