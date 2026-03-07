package com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 住院药学不合理用药 Excel 导出 Request VO，参数和 InpatientAdvicePageReqVO 是一致的")
@Data
public class InpatientAdviceExportReqVO {

    @Schema(description = "住院药学ID", example = "21616")
    private Long inpatientId;

    @Schema(description = "日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] adviceDate;

    @Schema(description = "医嘱内容")
    private String adviceContent;

    @Schema(description = "不合理原因", example = "不香")
    private String irrationalReason;

    @Schema(description = "用药建议")
    private String medicationSuggestion;

    @Schema(description = "结果")
    private String result;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
