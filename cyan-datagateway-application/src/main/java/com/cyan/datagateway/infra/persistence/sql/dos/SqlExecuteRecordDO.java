package com.cyan.datagateway.infra.persistence.sql.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SQL执行记录数据对象
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Data
@TableName("sql_execute_record")
public class SqlExecuteRecordDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String executeId;

    private Long userId;

    private String userName;

    private String sqlContent;

    private String sqlType;

    private String queryEngine;

    private String database;

    private String status;

    private Long rowCount;

    private Long costTimeMs;

    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 执行来源
     */
    private String source;
}
