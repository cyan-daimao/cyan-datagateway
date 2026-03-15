package com.cyan.datagateway.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SQL执行状态枚举
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Getter
@AllArgsConstructor
public enum SqlExecuteStatus {

    PENDING("PENDING", "待执行"),

    RUNNING("RUNNING", "执行中"),

    SUCCESS("SUCCESS", "执行成功"),

    FAILED("FAILED", "执行失败"),

    TIMEOUT("TIMEOUT", "执行超时"),

    CANCELLED("CANCELLED", "已取消"),
    ;

    private final String code;
    private final String desc;

    public static SqlExecuteStatus getByCode(String code) {
        for (SqlExecuteStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}
