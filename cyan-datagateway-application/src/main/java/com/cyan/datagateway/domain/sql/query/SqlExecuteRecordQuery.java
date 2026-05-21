package com.cyan.datagateway.domain.sql.query;

import com.cyan.arch.common.api.Pagination;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * SQL执行记录查询对象
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SqlExecuteRecordQuery extends Pagination {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * SQL类型
     */
    private String sqlType;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 执行ID
     */
    private String executeId;

    /**
     * 开始时间起
     */
    private LocalDateTime startTimeBegin;

    /**
     * 开始时间止
     */
    private LocalDateTime startTimeEnd;
}
