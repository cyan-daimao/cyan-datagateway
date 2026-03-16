package com.cyan.datagateway.application.sql.bo;

import com.cyan.datagateway.enums.SqlExecuteStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * SQL执行结果BO
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SqlExecuteResultBO {

    /**
     * 执行ID
     */
    private String executeId;

    /**
     * 执行状态
     */
    private SqlExecuteStatus status;

    /**
     * 耗时
     */
    private Long costTimeMs;

    /**
     * 数据
     */
    private List<Map<String, Object>> data;

    /**
     * 错误信息
     */
    private String errorMessage;


}
