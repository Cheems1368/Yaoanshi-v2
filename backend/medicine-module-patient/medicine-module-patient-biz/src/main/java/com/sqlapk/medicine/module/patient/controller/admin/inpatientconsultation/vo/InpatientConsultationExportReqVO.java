package com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 主要用药咨询 Excel 导出 Request VO，参数和 InpatientConsultationPageReqVO 是一致的")
@Data
public class InpatientConsultationExportReqVO {

    @Schema(description = "住院药学ID", example = "17193")
    private Long inpatientId;

    @Schema(description = "日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] consultationDate;

    @Schema(description = "咨询要点")
    private String consultationPoints;

    @Schema(description = "答复内容")
    private String replyContent;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
