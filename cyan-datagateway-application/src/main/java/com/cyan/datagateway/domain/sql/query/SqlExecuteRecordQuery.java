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

    private Long userId;

    private String userName;

    private String sqlType;

    private String status;

    private String executeId;

    private LocalDateTime startTimeBegin;

    private LocalDateTime startTimeEnd;
}
