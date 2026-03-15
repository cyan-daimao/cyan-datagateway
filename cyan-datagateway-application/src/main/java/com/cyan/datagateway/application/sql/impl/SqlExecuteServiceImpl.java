package com.cyan.datagateway.application.sql.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.Page as ApiPage;
import com.cyan.arch.common.util.Snowflake;
import com.cyan.datagateway.application.sql.SqlExecuteService;
import com.cyan.datagateway.application.sql.bo.SqlExecuteRecordBO;
import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd;
import com.cyan.datagateway.application.sql.convert.SqlExecuteRecordAppConvert;
import com.cyan.datagateway.domain.sql.SqlExecuteRecord;
import com.cyan.datagateway.domain.sql.query.SqlExecuteRecordQuery;
import com.cyan.datagateway.domain.sql.repository.SqlExecuteRecordRepository;
import com.cyan.datagateway.enums.SqlType;
import com.cyan.datagateway.infra.util.StarRocksUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * SQL执行服务实现
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SqlExecuteServiceImpl implements SqlExecuteService {

    private final StarRocksUtil starRocksUtil;
    private final SqlExecuteRecordRepository recordRepository;
    private final Snowflake snowflake = new Snowflake(1, 1);

    @Override
    public SqlExecuteResultBO executeQuery(SqlExecuteCmd cmd, Long userId, String userName) {
        String executeId = generateExecuteId();
        long startTime = System.currentTimeMillis();

        SqlExecuteRecord record = SqlExecuteRecord.create(
                executeId, userId, userName, cmd.getSql(), "STARROCKS", cmd.getDatabase()
        );
        recordRepository.save(record);

        try {
            Assert.isTrue(isSafeQuery(cmd.getSql()), "只允许执行SELECT查询语句");

            List<Map<String, Object>> data = starRocksUtil.queryForList(cmd.getSql());
            long costTime = System.currentTimeMillis() - startTime;

            record.success(data != null ? (long) data.size() : 0L, costTime);
            recordRepository.update(record);

            return SqlExecuteResultBO.success(executeId, data, costTime);
        } catch (SQLException e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("SQL执行失败: {}", e.getMessage(), e);
            record.fail(e.getMessage(), costTime);
            recordRepository.update(record);
            return SqlExecuteResultBO.fail(executeId, e.getMessage(), costTime);
        }
    }

    @Override
    public SqlExecuteResultBO executeUpdate(SqlExecuteCmd cmd, Long userId, String userName) {
        String executeId = generateExecuteId();
        long startTime = System.currentTimeMillis();

        SqlExecuteRecord record = SqlExecuteRecord.create(
                executeId, userId, userName, cmd.getSql(), "STARROCKS", cmd.getDatabase()
        );
        recordRepository.save(record);

        try {
            SqlType sqlType = SqlType.parseFromSql(cmd.getSql());
            Assert.isTrue(isWriteOperation(sqlType), "只允许执行INSERT/UPDATE/DELETE语句");

            int affectedRows = starRocksUtil.executeUpdate(cmd.getSql());
            long costTime = System.currentTimeMillis() - startTime;

            record.success((long) affectedRows, costTime);
            recordRepository.update(record);

            return SqlExecuteResultBO.success(executeId, null, costTime);
        } catch (SQLException e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("SQL执行失败: {}", e.getMessage(), e);
            record.fail(e.getMessage(), costTime);
            recordRepository.update(record);
            return SqlExecuteResultBO.fail(executeId, e.getMessage(), costTime);
        }
    }

    @Override
    public ApiPage<SqlExecuteRecordBO> page(SqlExecuteRecordQuery query) {
        Page<SqlExecuteRecord> page = recordRepository.page(query);
        return SqlExecuteRecordAppConvert.INSTANCE.toApiPage(page);
    }

    @Override
    public SqlExecuteRecordBO getByExecuteId(String executeId) {
        SqlExecuteRecord record = recordRepository.findByExecuteId(executeId);
        return record != null ? SqlExecuteRecordAppConvert.INSTANCE.toBO(record) : null;
    }

    @Override
    public List<SqlExecuteRecordBO> getRecentByUserId(Long userId, Integer limit) {
        List<SqlExecuteRecord> records = recordRepository.findByUserId(userId, limit);
        return SqlExecuteRecordAppConvert.INSTANCE.toBOList(records);
    }

    private String generateExecuteId() {
        return "SQL" + snowflake.nextId();
    }

    private boolean isSafeQuery(String sql) {
        String upperSql = sql.trim().toUpperCase();
        return upperSql.startsWith("SELECT") || upperSql.startsWith("SHOW") || upperSql.startsWith("DESC") || upperSql.startsWith("EXPLAIN");
    }

    private boolean isWriteOperation(SqlType sqlType) {
        return sqlType == SqlType.INSERT || sqlType == SqlType.UPDATE || sqlType == SqlType.DELETE;
    }
}
