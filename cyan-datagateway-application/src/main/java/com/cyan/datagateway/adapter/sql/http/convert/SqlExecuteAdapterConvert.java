package com.cyan.datagateway.adapter.sql.http.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datagateway.adapter.sql.http.dto.SqlExecuteResultDTO;
import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * SQL执行适配层转换器
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public interface SqlExecuteAdapterConvert {

    SqlExecuteAdapterConvert INSTANCE = Mappers.getMapper(SqlExecuteAdapterConvert.class);

    SqlExecuteResultDTO toSqlExecuteResultDTO(SqlExecuteResultBO sqlExecuteResultBO);
}
