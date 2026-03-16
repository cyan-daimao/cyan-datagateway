package com.cyan.datagateway.adapter.sql.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datagateway.adapter.sql.http.convert.SqlExecuteAdapterConvert;
import com.cyan.datagateway.adapter.sql.http.dto.SqlExecuteResultDTO;
import com.cyan.datagateway.application.sql.SqlExecuteService;
import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * SQL执行控制器
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@RestController
@RequestMapping("/api/v1/sql")
@RequiredArgsConstructor
public class SqlExecuteController {

    private final SqlExecuteService sqlExecuteService;

    /**
     * 执行查询SQL
     */
    @PostMapping("/execute")
    public Response<SqlExecuteResultDTO> executeQuery(@Valid @RequestBody SqlExecuteCmd cmd) {
        SqlExecuteResultBO execute = sqlExecuteService.execute(cmd);
        SqlExecuteResultDTO resultDTO = SqlExecuteAdapterConvert.INSTANCE.toSqlExecuteResultDTO(execute);
        return Response.success(resultDTO);
    }

}
