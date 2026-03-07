package com.sqlapk.medicine.module.medication.controller.admin.integration.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 报告生成验证请求VO
 */
@Schema(description = "报告生成验证请求")
@Data
public class ReportGenerationValidationReqVO {

    @Schema(description = "患者ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "报告类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "报告类型不能为空")
    private Integer reportType;
}
