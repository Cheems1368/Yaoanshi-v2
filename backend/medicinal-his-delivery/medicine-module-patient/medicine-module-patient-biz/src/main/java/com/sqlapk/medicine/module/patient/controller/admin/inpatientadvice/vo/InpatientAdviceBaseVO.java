package com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 住院药学不合理用药 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class InpatientAdviceBaseVO {

    @Schema(description = "住院药学ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21616")
    @NotNull(message = "住院药学ID不能为空")
    private Long inpatientId;

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "日期不能为空")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime adviceDate;

    @Schema(description = "医嘱内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "医嘱内容不能为空")
    private String adviceContent;

    @Schema(description = "不合理原因", example = "不香")
    private String irrationalReason;

    @Schema(description = "用药建议")
    private String medicationSuggestion;

    @Schema(description = "结果")
    private String result;

}
