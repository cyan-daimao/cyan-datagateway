package com.cyan.datagateway.application.sql.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datagateway.application.sql.bo.SqlExecuteRecordBO;
import com.cyan.datagateway.domain.sql.SqlExecuteRecord;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * SQL执行记录应用层转换器
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public interface SqlExecuteRecordAppConvert {

    SqlExecuteRecordAppConvert INSTANCE = Mappers.getMapper(SqlExecuteRecordAppConvert.class);

    SqlExecuteRecordBO toBO(SqlExecuteRecord record);

    List<SqlExecuteRecordBO> toBOList(List<SqlExecuteRecord> records);

    default com.cyan.arch.common.api.Page<SqlExecuteRecordBO> toApiPage(Page<SqlExecuteRecord> page) {
        com.cyan.arch.common.api.Page<SqlExecuteRecordBO> apiPage = new com.cyan.arch.common.api.Page<>();
        apiPage.setCurrent((int) page.getCurrent());
        apiPage.setSize((int) page.getSize());
        apiPage.setTotal(page.getTotal());
//        apiPage.set((int) page.getPages());
        apiPage.setData(toBOList(page.getRecords()));
        return apiPage;
    }
}
