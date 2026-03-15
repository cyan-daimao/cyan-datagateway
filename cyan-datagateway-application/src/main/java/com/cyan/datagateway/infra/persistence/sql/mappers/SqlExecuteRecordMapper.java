package com.cyan.datagateway.infra.persistence.sql.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datagateway.infra.persistence.sql.dos.SqlExecuteRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * SQL执行记录Mapper
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Mapper
public interface SqlExecuteRecordMapper extends BaseMapper<SqlExecuteRecordDO> {
}
