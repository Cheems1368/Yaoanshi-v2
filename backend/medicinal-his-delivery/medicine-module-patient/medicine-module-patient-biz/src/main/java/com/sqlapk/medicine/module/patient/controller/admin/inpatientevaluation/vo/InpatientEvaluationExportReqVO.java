package com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 住院药学用药评估 Excel 导出 Request VO，参数和 InpatientEvaluationPageReqVO 是一致的")
@Data
public class InpatientEvaluationExportReqVO {

    @Schema(description = "住院药学ID", example = "515")
    private Long inpatientId;

    @Schema(description = "日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] evaluationDate;

    @Schema(description = "目标药物")
    private String targetDrug;

    @Schema(description = "症状")
    private String symptom;

    @Schema(description = "结果")
    private String result;

    @Schema(description = "用药建议")
    private String medicationSuggestion;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
