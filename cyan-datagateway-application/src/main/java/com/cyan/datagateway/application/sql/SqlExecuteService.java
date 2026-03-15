package com.cyan.datagateway.application.sql;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.arch.common.api.Page as ApiPage;
import com.cyan.datagateway.application.sql.bo.SqlExecuteRecordBO;
import com.cyan.datagateway.application.sql.bo.SqlExecuteResultBO;
import com.cyan.datagateway.application.sql.cmd.SqlExecuteCmd;
import com.cyan.datagateway.domain.sql.query.SqlExecuteRecordQuery;

/**
 * SQL执行服务接口
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
public interface SqlExecuteService {

    /**
     * 执行SQL查询
     *
     * @param cmd    SQL执行命令
     * @param userId 用户ID
     * @param userName 用户名
     * @return 执行结果
     */
    SqlExecuteResultBO executeQuery(SqlExecuteCmd cmd, Long userId, String userName);

    /**
     * 执行SQL更新
     *
     * @param cmd    SQL执行命令
     * @param userId 用户ID
     * @param userName 用户名
     * @return 执行结果
     */
    SqlExecuteResultBO executeUpdate(SqlExecuteCmd cmd, Long userId, String userName);

    /**
     * 分页查询执行记录
     */
    ApiPage<SqlExecuteRecordBO> page(SqlExecuteRecordQuery query);

    /**
     * 根据执行ID查询结果
     */
    SqlExecuteRecordBO getByExecuteId(String executeId);

    /**
     * 获取用户最近的执行记录
     */
    java.util.List<SqlExecuteRecordBO> getRecentByUserId(Long userId, Integer limit);
}
