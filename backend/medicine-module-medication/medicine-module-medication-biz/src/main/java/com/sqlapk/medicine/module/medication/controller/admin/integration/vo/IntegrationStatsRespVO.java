package com.sqlapk.medicine.module.medication.controller.admin.integration.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 集成服务统计响应VO
 */
@Schema(description = "集成服务统计响应")
@Data
public class IntegrationStatsRespVO {

    @Schema(description = "服务名称", example = "medication-plan-validation")
    private String serviceName;

    @Schema(description = "总调用次数", example = "1000")
    private long totalCalls;

    @Schema(description = "成功调用次数", example = "950")
    private long successCalls;

    @Schema(description = "失败调用次数", example = "50")
    private long failureCalls;

    @Schema(description = "成功率", example = "0.95")
    private double successRate;

    @Schema(description = "最后调用时间", example = "1640995200000")
    private long lastCallTime;
}
