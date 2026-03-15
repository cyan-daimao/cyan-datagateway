package com.cyan.datagateway.domain.sql.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.datagateway.domain.sql.SqlExecuteRecord;
import com.cyan.datagateway.domain.sql.query.SqlExecuteRecordQuery;

import java.util.List;

/**
 * SQL执行记录仓库接口
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
public interface SqlExecuteRecordRepository {

    /**
     * 保存SQL执行记录
     */
    void save(SqlExecuteRecord record);

    /**
     * 更新SQL执行记录
     */
    void update(SqlExecuteRecord record);

    /**
     * 根据ID查询
     */
    SqlExecuteRecord findById(Long id);

    /**
     * 根据执行ID查询
     */
    SqlExecuteRecord findByExecuteId(String executeId);

    /**
     * 分页查询
     */
    Page<SqlExecuteRecord> page(SqlExecuteRecordQuery query);

    /**
     * 根据用户ID查询历史记录
     */
    List<SqlExecuteRecord> findByUserId(Long userId, Integer limit);
}
