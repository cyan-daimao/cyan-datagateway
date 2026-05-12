package com.cyan.datagateway.infra.config;

import com.cyan.datagateway.domain.sql.valobj.DataPermission;
import com.cyan.datagateway.domain.sql.repository.DataPermissionRepository;
import org.springframework.stereotype.Repository;

/**
 * 数据权限仓库实现 - 预留实现
 * TODO: 后续对接实际的数据权限系统
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Repository
public class DataPermissionRepositoryImpl implements DataPermissionRepository {

    @Override
    public DataPermission findByUserId(Long userId) {
        // TODO: 后续从数据库或权限系统获取
        DataPermission permission = new DataPermission();
        permission.setUserId(userId);
        permission.setPermissionType(DataPermission.PermissionType.ALL);
        return permission;
    }

    @Override
    public void save(DataPermission permission) {
        // TODO: 后续实现保存逻辑
    }

    @Override
    public boolean checkPermission(Long userId, String database, String table) {
        // datagateway 不再做权限校验，只负责执行
        // 权限校验已上移到 cyan-dataauth
        return true;
    }
}
