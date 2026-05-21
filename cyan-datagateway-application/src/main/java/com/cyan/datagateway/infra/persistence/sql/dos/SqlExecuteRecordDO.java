package com.cyan.datagateway.infra.persistence.sql.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 执行ID
     */
    private String executeId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * SQL内容
     */
    private String sqlContent;

    /**
     * SQL类型
     */
    private String sqlType;

    /**
     * 查询引擎
     */
    private String queryEngine;

    /**
     * 目标数据库
     */
    private String database;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 影响行数/返回行数
     */
    private Long rowCount;

    /**
     * 执行耗时(毫秒)
     */
    private Long costTimeMs;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    private LocalDateTime updatedAt;

    /**
     * 执行来源
     */
    @TableField("source")
    private String source;

    /**
     * 删除时间
     */
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
