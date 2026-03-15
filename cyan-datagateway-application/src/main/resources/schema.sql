-- SQL执行记录表
CREATE TABLE IF NOT EXISTS `sql_execute_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `execute_id` VARCHAR(64) NOT NULL COMMENT '执行ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `user_name` VARCHAR(128) DEFAULT NULL COMMENT '用户名',
    `sql_content` TEXT NOT NULL COMMENT 'SQL内容',
    `sql_type` VARCHAR(32) DEFAULT NULL COMMENT 'SQL类型(SELECT/INSERT/UPDATE/DELETE等)',
    `query_engine` VARCHAR(32) DEFAULT 'STARROCKS' COMMENT '查询引擎',
    `database` VARCHAR(128) DEFAULT NULL COMMENT '目标数据库',
    `status` VARCHAR(32) NOT NULL COMMENT '执行状态',
    `row_count` BIGINT DEFAULT 0 COMMENT '影响行数/返回行数',
    `cost_time_ms` BIGINT DEFAULT 0 COMMENT '执行耗时(毫秒)',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_execute_id` (`execute_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL执行记录表';

-- 数据权限配置表（预留）
CREATE TABLE IF NOT EXISTS `data_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(128) DEFAULT NULL COMMENT '用户名',
    `permission_type` VARCHAR(32) NOT NULL DEFAULT 'ALL' COMMENT '权限类型(ALL/WHITELIST/BLACKLIST)',
    `allowed_databases` TEXT DEFAULT NULL COMMENT '允许访问的数据库(JSON数组)',
    `allowed_tables` TEXT DEFAULT NULL COMMENT '允许访问的表(JSON数组)',
    `denied_tables` TEXT DEFAULT NULL COMMENT '禁止访问的表(JSON数组)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据权限配置表';
