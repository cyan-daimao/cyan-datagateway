package com.cyan.datagateway.domain.sql.repository;

import com.cyan.datagateway.domain.sql.valobj.DataPermission;

/**
 * 数据权限仓库接口
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
public interface DataPermissionRepository {

    /**
     * 根据用户ID获取数据权限
     *
     * @param userId 用户ID
     * @return 数据权限配置
     */
    DataPermission findByUserId(Long userId);

    /**
     * 保存数据权限配置
     *
     * @param permission 数据权限配置
     */
    void save(DataPermission permission);

    /**
     * 检查用户是否有指定表的访问权限
     *
     * @param userId   用户ID
     * @param database 数据库
     * @param table    表名
     * @return 是否有权限
     */
    boolean checkPermission(Long userId, String database, String table);
}
