package com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 居家用药教育 Excel 导出 Request VO，参数和 HomeEducationPageReqVO 是一致的")
@Data
public class HomeEducationExportReqVO {

    @Schema(description = "住院药学ID", example = "6301")
    private Long homeId;

    @Schema(description = "日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] educationDate;

    @Schema(description = "诊断与用药")
    private String diagnosisAndMedication;

    @Schema(description = "要点")
    private String keyPoints;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
