package com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 住院药学用药评估 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InpatientEvaluationBaseVO {

    @Schema(description = "住院药学ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "515")
    @NotNull(message = "住院药学ID不能为空")
    private Long inpatientId;

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "日期不能为空")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime evaluationDate;

    @Schema(description = "目标药物", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标药物不能为空")
    private String targetDrug;

    @Schema(description = "症状")
    private String symptom;

    @Schema(description = "结果")
    private String result;

    @Schema(description = "用药建议")
    private String medicationSuggestion;

}
