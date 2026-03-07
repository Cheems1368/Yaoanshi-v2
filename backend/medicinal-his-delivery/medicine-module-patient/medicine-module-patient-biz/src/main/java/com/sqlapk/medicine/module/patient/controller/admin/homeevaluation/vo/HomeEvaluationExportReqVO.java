package com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 居家随访评估 Excel 导出 Request VO，参数和 HomeEvaluationPageReqVO 是一致的")
@Data
public class HomeEvaluationExportReqVO {

    @Schema(description = "日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] evaluationDate;

    @Schema(description = "住院药学ID", example = "13958")
    private Long homeId;

    @Schema(description = "日期")
    private String symptom;

    @Schema(description = "检查指标")
    private String indicator;

    @Schema(description = "治疗问题")
    private String treatmentProblem;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
