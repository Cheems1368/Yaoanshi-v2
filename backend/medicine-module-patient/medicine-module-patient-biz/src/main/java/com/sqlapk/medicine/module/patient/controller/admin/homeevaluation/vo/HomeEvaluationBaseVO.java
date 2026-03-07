package com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static com.sqlapk.medicine.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

/**
 * 居家随访评估 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class HomeEvaluationBaseVO {

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "日期不能为空")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime evaluationDate;

    @Schema(description = "住院药学ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13958")
    @NotNull(message = "住院药学ID不能为空")
    private Long homeId;

    @Schema(description = "症状")
    private String symptom;

    @Schema(description = "检查指标")
    private String indicator;

    @Schema(description = "治疗问题")
    private String treatmentProblem;

}
