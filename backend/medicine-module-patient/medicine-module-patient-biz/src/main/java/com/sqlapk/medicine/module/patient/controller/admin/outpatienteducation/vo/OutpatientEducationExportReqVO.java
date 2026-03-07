package com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 门诊处方用药教育明细 Excel 导出 Request VO，参数和 OutpatientEducationPageReqVO 是一致的")
@Data
public class OutpatientEducationExportReqVO {

    @Schema(description = "门诊药学ID", example = "24737")
    private Long outpatientId;

    @Schema(description = "教育ID", example = "32120")
    private Long educationId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
