package com.sqlapk.medicine.module.patient.controller.admin.answers.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 问卷 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class AnswersBaseVO {

    @Schema(description = "用户ID", example = "7786")
    private Long userId;

    private Long patientId;

    @Schema(description = "问题ID", example = "18445")
    private Long questionId;

    @Schema(description = "选项ID", example = "18404")
    private Long optionId;

    @Schema(description = "答案值")
    private Double value;

    private String content;

}
