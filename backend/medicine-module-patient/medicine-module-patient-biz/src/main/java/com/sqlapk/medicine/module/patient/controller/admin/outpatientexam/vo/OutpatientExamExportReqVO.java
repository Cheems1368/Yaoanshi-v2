package com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 门诊药学检查项目明细 Excel 导出 Request VO，参数和 OutpatientExamPageReqVO 是一致的")
@Data
public class OutpatientExamExportReqVO {

    @Schema(description = "门诊药学ID", example = "14932")
    private Long outpatientId;

    @Schema(description = "检查项目ID", example = "25794")
    private Long examinationsId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
