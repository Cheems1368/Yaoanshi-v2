package com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 居家用药咨询 Excel 导出 Request VO，参数和 HomeConsultationPageReqVO 是一致的")
@Data
public class HomeConsultationExportReqVO {

    @Schema(description = "住院药学ID", example = "8422")
    private Long homeId;

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
