package com.cyan.datagateway.adapter.sql.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datagateway.adapter.sql.http.convert.SqlExecuteAdapterConvert;
import com.cyan.datagateway.adapter.sql.http.dto.SqlExecuteResultDTO;
import com.cyan.datagateway.application.sql.SqlExecuteService;
import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Metric SQL执行控制器
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@RestController
@RequestMapping("/api/v1/starrocks/metric")
@RequiredArgsConstructor
public class MetricSqlController {

    private final SqlExecuteService sqlExecuteService;

    /**
     * 执行指标分析SQL
     */
    @PostMapping("/execute")
    public Response<SqlExecuteResultDTO> executeQuery(@Valid @RequestBody SqlExecuteCmd cmd) {
        cmd.setSource("METRIC");
        SqlExecuteResultBO execute = sqlExecuteService.execute(cmd);
        SqlExecuteResultDTO resultDTO = SqlExecuteAdapterConvert.INSTANCE.toSqlExecuteResultDTO(execute);
        return Response.success(resultDTO);
    }

}
