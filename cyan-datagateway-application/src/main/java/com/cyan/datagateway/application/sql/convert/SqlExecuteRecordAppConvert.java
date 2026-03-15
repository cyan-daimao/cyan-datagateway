package com.cyan.datagateway.application.sql.convert;

import com.cyan.arch.common.api.Page as ApiPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
@Mapper
public interface SqlExecuteRecordAppConvert {

    SqlExecuteRecordAppConvert INSTANCE = Mappers.getMapper(SqlExecuteRecordAppConvert.class);

    SqlExecuteRecordBO toBO(SqlExecuteRecord record);

    List<SqlExecuteRecordBO> toBOList(List<SqlExecuteRecord> records);

    default ApiPage<SqlExecuteRecordBO> toApiPage(Page<SqlExecuteRecord> page) {
        ApiPage<SqlExecuteRecordBO> apiPage = new ApiPage<>();
        apiPage.setPageNum((int) page.getCurrent());
        apiPage.setPageSize((int) page.getSize());
        apiPage.setTotal(page.getTotal());
        apiPage.setPages((int) page.getPages());
        apiPage.setRecords(toBOList(page.getRecords()));
        return apiPage;
    }
}
