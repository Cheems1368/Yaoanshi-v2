package com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 门诊药学检查项目明细 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class OutpatientExamBaseVO {

    @Schema(description = "门诊药学ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14932")
    @NotNull(message = "门诊药学ID不能为空")
    private Long outpatientId;

    @Schema(description = "检查项目ID", example = "25794")
    private Long examinationsId;

}
