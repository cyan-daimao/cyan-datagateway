package com.cyan.datagateway.application.sql.bo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * SQL执行结果BO
 *
 * @author cy.Y
 * @since 1.0-SNAPSHOT
 */
@Data
public class SqlExecuteResultBO {

    private String executeId;

    private String status;

    private Long rowCount;

    private Long costTimeMs;

    private List<Map<String, Object>> data;

    private String errorMessage;

    public static SqlExecuteResultBO success(String executeId, List<Map<String, Object>> data, Long costTimeMs) {
        SqlExecuteResultBO result = new SqlExecuteResultBO();
        result.setExecuteId(executeId);
        result.setStatus("SUCCESS");
        result.setData(data);
        result.setRowCount(data != null ? (long) data.size() : 0L);
        result.setCostTimeMs(costTimeMs);
        return result;
    }

    public static SqlExecuteResultBO fail(String executeId, String errorMessage, Long costTimeMs) {
        SqlExecuteResultBO result = new SqlExecuteResultBO();
        result.setExecuteId(executeId);
        result.setStatus("FAILED");
        result.setErrorMessage(errorMessage);
        result.setCostTimeMs(costTimeMs);
        return result;
    }
}
