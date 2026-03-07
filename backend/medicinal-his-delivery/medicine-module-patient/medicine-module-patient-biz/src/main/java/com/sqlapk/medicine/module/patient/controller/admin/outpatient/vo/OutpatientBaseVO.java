package com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 门诊药学服务 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class OutpatientBaseVO {

    @Schema(description = "医生ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22876")
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    @Schema(description = "患者ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17296")
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @Schema(description = "过敏史")
    private String allergyHistory;

    @Schema(description = "患病史")
    private String medicalHistory;

    @Schema(description = "家族史")
    private String familyHistory;

    @Schema(description = "个人史")
    private String personalHistory;

    @Schema(description = "备注")
    private String comment;

}
