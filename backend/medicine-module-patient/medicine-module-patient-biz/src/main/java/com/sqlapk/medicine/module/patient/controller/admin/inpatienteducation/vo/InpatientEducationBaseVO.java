package com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 住院用药教育 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class InpatientEducationBaseVO {

    @Schema(description = "住院药学ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8658")
    @NotNull(message = "住院药学ID不能为空")
    private Long inpatientId;

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "日期不能为空")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime educationDate;

    @Schema(description = "诊断与用药", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "诊断与用药不能为空")
    private String diagnosisAndMedication;

    @Schema(description = "要点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "要点不能为空")
    private String keyPoints;

}
