package com.sqlapk.medicine.module.medication.controller.admin.integration.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用药计划验证响应VO
 */
@Schema(description = "用药计划验证响应")
@Data
public class MedicationPlanValidationRespVO {

    @Schema(description = "验证是否通过", example = "true")
    private boolean valid;

    @Schema(description = "患者信息是否有效", example = "true")
    private boolean patientValid;

    @Schema(description = "药品信息是否有效", example = "true")
    private boolean drugValid;

    @Schema(description = "用户信息是否有效", example = "true")
    private boolean userValid;

    @Schema(description = "是否可以修改", example = "true")
    private boolean canModify;

    @Schema(description = "药品是否可用", example = "true")
    private boolean drugAvailable;

    @Schema(description = "是否为管制药品", example = "false")
    private boolean drugControlled;

    @Schema(description = "患者档案是否通过审核", example = "true")
    private boolean patientAuditPassable;

    @Schema(description = "患者档案完整度评分", example = "85.5")
    private Double patientCompletenessScore;

    @Schema(description = "过敏风险等级", example = "LOW")
    private String allergyRiskLevel;

    @Schema(description = "过敏风险描述", example = "无已知过敏风险")
    private String allergyRiskDescription;

    @Schema(description = "错误信息列表")
    private List<String> errors;

    @Schema(description = "警告信息列表")
    private List<String> warnings;

    @Schema(description = "验证时间")
    private LocalDateTime validationTime;

    // 防御性副本方法
    public List<String> getErrors() {
        return errors != null ? new java.util.ArrayList<>(errors) : null;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors != null ? new java.util.ArrayList<>(errors) : null;
    }

    public List<String> getWarnings() {
        return warnings != null ? new java.util.ArrayList<>(warnings) : null;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? new java.util.ArrayList<>(warnings) : null;
    }
}
