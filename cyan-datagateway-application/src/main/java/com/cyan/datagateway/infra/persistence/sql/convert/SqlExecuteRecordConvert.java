package com.cyan.datagateway.infra.persistence.sql.convert;

import com.cyan.arch.base.mapstruct.MapstructConvert;
import com.cyan.datagateway.domain.sql.SqlExecuteRecord;
import com.cyan.datagateway.infra.persistence.sql.dos.SqlExecuteRecordDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * SQL执行记录转换器
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public interface SqlExecuteRecordConvert {

    SqlExecuteRecordConvert INSTANCE = Mappers.getMapper(SqlExecuteRecordConvert.class);

    @Mapping(target = "sqlType", expression = "java(record.getSqlType() != null ? record.getSqlType().getCode() : null)")
    @Mapping(target = "status", expression = "java(record.getStatus() != null ? record.getStatus().getCode() : null)")
    SqlExecuteRecordDO toDO(SqlExecuteRecord record);

    @Mapping(target = "sqlType", expression = "java(com.cyan.datagateway.enums.SqlType.getByCode(dO.getSqlType()))")
    @Mapping(target = "status", expression = "java(com.cyan.datagateway.enums.SqlExecuteStatus.getByCode(dO.getStatus()))")
    SqlExecuteRecord toDomain(SqlExecuteRecordDO dO);
}
