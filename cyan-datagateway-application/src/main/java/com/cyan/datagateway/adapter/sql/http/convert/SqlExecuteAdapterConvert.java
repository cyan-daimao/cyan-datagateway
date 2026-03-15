package com.cyan.datagateway.adapter.sql.http.convert;

import com.cyan.arch.common.api.Page;
import com.cyan.datagateway.adapter.sql.http.dto.SqlExecuteCmdDTO;
import com.cyan.datagateway.adapter.sql.http.dto.SqlExecuteRecordDTO;
import com.cyan.datagateway.adapter.sql.http.dto.SqlExecuteResultDTO;
import com.cyan.datagateway.application.sql.bo.SqlExecuteRecordBO;
import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd;
import com.cyan.datagateway.domain.sql.query.SqlExecuteRecordQuery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * SQL执行适配层转换器
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Mapper
public interface SqlExecuteAdapterConvert {

    SqlExecuteAdapterConvert INSTANCE = Mappers.getMapper(SqlExecuteAdapterConvert.class);

    SqlExecuteCmd toCmd(SqlExecuteCmdDTO dto);

    SqlExecuteResultDTO toResultDTO(SqlExecuteResultBO bo);

    SqlExecuteRecordDTO toRecordDTO(SqlExecuteRecordBO bo);

    List<SqlExecuteRecordDTO> toRecordDTOList(List<SqlExecuteRecordBO> bos);

    @Mapping(target = "pageNum", source = "pageNum")
    @Mapping(target = "pageSize", source = "pageSize")
    SqlExecuteRecordQuery toQuery(com.cyan.datagateway.adapter.sql.http.dto.SqlExecuteRecordQueryDTO dto);

    default Page<SqlExecuteRecordDTO> toRecordPage(Page<SqlExecuteRecordBO> boPage) {
        Page<SqlExecuteRecordDTO> dtoPage = new Page<>();
        dtoPage.setPageNum(boPage.getPageNum());
        dtoPage.setPageSize(boPage.getPageSize());
        dtoPage.setTotal(boPage.getTotal());
        dtoPage.setPages(boPage.getPages());
        dtoPage.setRecords(toRecordDTOList(boPage.getRecords()));
        return dtoPage;
    }
}
