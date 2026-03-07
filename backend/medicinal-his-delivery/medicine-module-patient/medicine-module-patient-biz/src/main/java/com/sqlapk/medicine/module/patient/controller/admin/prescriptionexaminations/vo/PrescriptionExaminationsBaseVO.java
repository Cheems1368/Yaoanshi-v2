package com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 处方检查项目关系 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PrescriptionExaminationsBaseVO {

    @Schema(description = "处方ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7737")
    @NotNull(message = "处方ID不能为空")
    private Long prescriptionId;

    @Schema(description = "检查项目ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12994")
    @NotNull(message = "检查项目ID不能为空")
    private Long examinationId;

    @Schema(description = "检查结果")
    private String result;

}
