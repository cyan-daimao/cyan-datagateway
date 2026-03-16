package com.cyan.datagateway.adapter.sql.http.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * SQL执行结果DTO
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SqlExecuteResultDTO {

    /**
     * 执行ID
     */
    private String executeId;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 执行耗时
     */
    private Long costTimeMs;

    /**
     * 执行结果数据
     */
    private List<Map<String, Object>> data;

    /**
     * 执行错误信息
     */
    private String errorMessage;

}
