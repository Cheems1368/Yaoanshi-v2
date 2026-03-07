package com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 住院药学查房记录 Excel 导出 Request VO，参数和 InpatientRoundPageReqVO 是一致的")
@Data
public class InpatientRoundExportReqVO {

    @Schema(description = "住院药学ID", example = "24238")
    private Long inpatientId;

    @Schema(description = "日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] roundDate;

    @Schema(description = "查房内容")
    private String roundContent;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
