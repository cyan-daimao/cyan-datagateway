package com.cyan.datagateway.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询引擎类型枚举
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Getter
@AllArgsConstructor
public enum QueryEngineType {

    STARROCKS("STARROCKS", "StarRocks"),

    PRESTO("PRESTO", "Presto"),

    TRINO("TRINO", "Trino"),

    SPARK("SPARK", "Spark SQL"),
    ;

    private final String code;
    private final String desc;

    public static QueryEngineType getByCode(String code) {
        for (QueryEngineType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
