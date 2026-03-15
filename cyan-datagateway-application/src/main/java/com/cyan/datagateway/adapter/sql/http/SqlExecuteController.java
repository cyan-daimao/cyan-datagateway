package com.cyan.datagateway.adapter.sql.http;

import com.cyan.arch.common.api.Page;
import com.cyan.arch.common.api.Response;
import com.cyan.datagateway.adapter.sql.http.convert.SqlExecuteAdapterConvert;
import com.cyan.datagateway.adapter.sql.http.dto.SqlExecuteCmdDTO;
import com.cyan.datagateway.adapter.sql.http.dto.SqlExecuteDTO;
import com.cyan.datagateway.adapter.sql.http.dto.SqlExecuteRecordDTO;
import com.cyan.datagateway.adapter.sql.http.dto.SqlExecuteRecordQueryDTO;
import com.cyan.datagateway.adapter.sql.http.dto.SqlExecuteResultDTO;
import com.cyan.datagateway.application.sql.SqlExecuteService;
import com.cyan.datagateway.application.sql.bo.SqlExecuteRecordBO;
import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd;
import com.cyan.datagateway.domain.sql.query.SqlExecuteRecordQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping("/query")
    public Response<SqlExecuteResultDTO> executeQuery(
            @Valid @RequestBody SqlExecuteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        
        SqlExecuteCmd cmd = SqlExecuteAdapterConvert.INSTANCE.toCmd(dto);
        SqlExecuteResultBO result = sqlExecuteService.executeQuery(cmd, userId, userName);
        return Response.success(SqlExecuteAdapterConvert.INSTANCE.toResultDTO(result));
    }

    /**
     * 执行更新SQL (INSERT/UPDATE/DELETE)
     */
    @PostMapping("/update")
    public Response<SqlExecuteResultDTO> executeUpdate(
            @Valid @RequestBody SqlExecuteCmdDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        
        SqlExecuteCmd cmd = SqlExecuteAdapterConvert.INSTANCE.toCmd(dto);
        SqlExecuteResultBO result = sqlExecuteService.executeUpdate(cmd, userId, userName);
        return Response.success(SqlExecuteAdapterConvert.INSTANCE.toResultDTO(result));
    }

    /**
     * 分页查询执行记录
     */
    @GetMapping("/records")
    public Response<Page<SqlExecuteRecordDTO>> pageRecords(SqlExecuteRecordQueryDTO dto) {
        SqlExecuteRecordQuery query = SqlExecuteAdapterConvert.INSTANCE.toQuery(dto);
        Page<SqlExecuteRecordBO> boPage = sqlExecuteService.page(query);
        return Response.success(SqlExecuteAdapterConvert.INSTANCE.toRecordPage(boPage));
    }

    /**
     * 根据执行ID查询结果
     */
    @GetMapping("/records/{executeId}")
    public Response<SqlExecuteRecordDTO> getByExecuteId(@PathVariable String executeId) {
        SqlExecuteRecordBO record = sqlExecuteService.getByExecuteId(executeId);
        return Response.success(SqlExecuteAdapterConvert.INSTANCE.toRecordDTO(record));
    }

    /**
     * 获取当前用户最近的执行记录
     */
    @GetMapping("/records/recent")
    public Response<List<SqlExecuteRecordDTO>> getRecentRecords(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<SqlExecuteRecordBO> records = sqlExecuteService.getRecentByUserId(userId, limit);
        return Response.success(SqlExecuteAdapterConvert.INSTANCE.toRecordDTOList(records));
    }
}
