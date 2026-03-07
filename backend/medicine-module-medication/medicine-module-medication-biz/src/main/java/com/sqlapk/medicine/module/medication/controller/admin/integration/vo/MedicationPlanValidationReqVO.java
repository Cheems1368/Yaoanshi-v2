package com.sqlapk.medicine.module.medication.controller.admin.integration.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用药计划验证请求VO
 */
@Schema(description = "用药计划验证请求")
@Data
public class MedicationPlanValidationReqVO {

    @Schema(description = "患者ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @Schema(description = "药品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "药品ID不能为空")
    private Long drugId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
