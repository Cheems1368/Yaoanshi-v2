package com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 处方 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PrescriptionsBaseVO {

    @Schema(description = "患者ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "593")
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @Schema(description = "处方类型(门诊,住院,居家,药学)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "处方类型(门诊,住院,居家,药学)不能为空")
    private Integer type;

    @Schema(description = "医生", example = "31568")
    private Long doctorId;

    @Schema(description = "初步诊断")
    private String diagnosis;

}
