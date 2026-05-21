package com.cyan.datagateway.domain.sql.valobj;

import lombok.Data;

import java.util.List;

/**
 * 数据权限值对象
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Data
public class DataPermission {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 允许访问的数据库列表
     */
    private List<String> allowedDatabases;

    /**
     * 允许访问的表列表
     */
    private List<String> allowedTables;

    /**
     * 禁止访问的表列表
     */
    private List<String> deniedTables;

    /**
     * 权限类型
     */
    private PermissionType permissionType;

    public enum PermissionType {
        ALL,
        WHITELIST,
        BLACKLIST
    }

    public boolean hasPermission(String database, String table) {
        if (permissionType == PermissionType.ALL) {
            return true;
        }
        if (permissionType == PermissionType.WHITELIST) {
            boolean dbAllowed = allowedDatabases == null || allowedDatabases.isEmpty() 
                    || allowedDatabases.contains(database);
            boolean tableAllowed = allowedTables == null || allowedTables.isEmpty() 
                    || allowedTables.contains(database + "." + table);
            return dbAllowed && tableAllowed;
        }
        if (permissionType == PermissionType.BLACKLIST) {
            boolean dbDenied = allowedDatabases != null && allowedDatabases.contains(database);
            boolean tableDenied = deniedTables != null && deniedTables.contains(database + "." + table);
            return !dbDenied && !tableDenied;
        }
        return false;
    }
}
