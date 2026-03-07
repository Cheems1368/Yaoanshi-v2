package com.sqlapk.medicine.module.patient.controller.admin.education.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用药教育 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class EducationBaseVO {

    @Schema(description = "药品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotNull(message = "药品名称不能为空")
    private String medicineName;

    @Schema(description = "患者ID", example = "2884")
    private Long patientId;

    @Schema(description = "疾病")
    private String disease;

    @Schema(description = "教育内容")
    private String content;

    @Schema(description = "教育依据")
    private String contentBasis;

}
