package com.cyan.datagateway.infra.persistence.sql.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.datagateway.domain.sql.SqlExecuteRecord;
import com.cyan.datagateway.domain.sql.query.SqlExecuteRecordQuery;
import com.cyan.datagateway.domain.sql.repository.SqlExecuteRecordRepository;
import com.cyan.datagateway.infra.persistence.sql.convert.SqlExecuteRecordConvert;
import com.cyan.datagateway.infra.persistence.sql.dos.SqlExecuteRecordDO;
import com.cyan.datagateway.infra.persistence.sql.mappers.SqlExecuteRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SQL执行记录仓库实现
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Repository
@RequiredArgsConstructor
public class SqlExecuteRecordRepositoryImpl implements SqlExecuteRecordRepository {

    private final SqlExecuteRecordMapper mapper;

    @Override
    public void save(SqlExecuteRecord record) {
        SqlExecuteRecordDO dO = SqlExecuteRecordConvert.INSTANCE.toDO(record);
        mapper.insert(dO);
        record.setId(dO.getId());
    }

    @Override
    public void update(SqlExecuteRecord record) {
        SqlExecuteRecordDO dO = SqlExecuteRecordConvert.INSTANCE.toDO(record);
        mapper.updateById(dO);
    }

    @Override
    public SqlExecuteRecord findById(Long id) {
        SqlExecuteRecordDO dO = mapper.selectById(id);
        return dO != null ? SqlExecuteRecordConvert.INSTANCE.toDomain(dO) : null;
    }

    @Override
    public SqlExecuteRecord findByExecuteId(String executeId) {
        LambdaQueryWrapper<SqlExecuteRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SqlExecuteRecordDO::getExecuteId, executeId);
        SqlExecuteRecordDO dO = mapper.selectOne(wrapper);
        return dO != null ? SqlExecuteRecordConvert.INSTANCE.toDomain(dO) : null;
    }

    @Override
    public Page<SqlExecuteRecord> page(SqlExecuteRecordQuery query) {
        Page<SqlExecuteRecordDO> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SqlExecuteRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getUserId() != null, SqlExecuteRecordDO::getUserId, query.getUserId())
                .like(query.getUserName() != null, SqlExecuteRecordDO::getUserName, query.getUserName())
                .eq(query.getSqlType() != null, SqlExecuteRecordDO::getSqlType, query.getSqlType())
                .eq(query.getStatus() != null, SqlExecuteRecordDO::getStatus, query.getStatus())
                .eq(query.getExecuteId() != null, SqlExecuteRecordDO::getExecuteId, query.getExecuteId())
                .ge(query.getStartTimeBegin() != null, SqlExecuteRecordDO::getStartTime, query.getStartTimeBegin())
                .le(query.getStartTimeEnd() != null, SqlExecuteRecordDO::getStartTime, query.getStartTimeEnd())
                .orderByDesc(SqlExecuteRecordDO::getCreatedAt);

        Page<SqlExecuteRecordDO> resultPage = mapper.selectPage(page, wrapper);
        Page<SqlExecuteRecord> domainPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        domainPage.setRecords(resultPage.getRecords().stream()
                .map(SqlExecuteRecordConvert.INSTANCE::toDomain)
                .toList());
        return domainPage;
    }

    @Override
    public List<SqlExecuteRecord> findByUserId(Long userId, Integer limit) {
        LambdaQueryWrapper<SqlExecuteRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SqlExecuteRecordDO::getUserId, userId)
                .orderByDesc(SqlExecuteRecordDO::getCreatedAt)
                .last("LIMIT " + limit);
        return mapper.selectList(wrapper).stream()
                .map(SqlExecuteRecordConvert.INSTANCE::toDomain)
                .toList();
    }
}
