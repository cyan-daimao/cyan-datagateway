package com.cyan.datagateway.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SQL类型枚举
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Getter
@AllArgsConstructor
public enum SqlType {

    SELECT("SELECT", "查询"),

    INSERT("INSERT", "插入"),

    UPDATE("UPDATE", "更新"),

    DELETE("DELETE", "删除"),

    CREATE("CREATE", "创建"),

    ALTER("ALTER", "修改"),

    DROP("DROP", "删除"),

    OTHER("OTHER", "其他"),
    ;

    private final String code;
    private final String desc;

    public static SqlType getByCode(String code) {
        for (SqlType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return OTHER;
    }

    public static SqlType parseFromSql(String sql) {
        if (sql == null || sql.isBlank()) {
            return OTHER;
        }
        String upperSql = sql.trim().toUpperCase();
        if (upperSql.startsWith("SELECT")) {
            return SELECT;
        } else if (upperSql.startsWith("INSERT")) {
            return INSERT;
        } else if (upperSql.startsWith("UPDATE")) {
            return UPDATE;
        } else if (upperSql.startsWith("DELETE")) {
            return DELETE;
        } else if (upperSql.startsWith("CREATE")) {
            return CREATE;
        } else if (upperSql.startsWith("ALTER")) {
            return ALTER;
        } else if (upperSql.startsWith("DROP")) {
            return DROP;
        }
        return OTHER;
    }
}
