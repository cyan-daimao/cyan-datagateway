package com.cyan.datagateway.adapter.sql.rpc.convert;

import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.client.dto.SqlExecuteResultDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * SqlGateway RPC转换器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface SqlGatewayConvert {

    SqlGatewayConvert INSTANCE = Mappers.getMapper(SqlGatewayConvert.class);

    SqlExecuteResultDTO toSqlExecuteResultDTO(SqlExecuteResultBO bo);
}
