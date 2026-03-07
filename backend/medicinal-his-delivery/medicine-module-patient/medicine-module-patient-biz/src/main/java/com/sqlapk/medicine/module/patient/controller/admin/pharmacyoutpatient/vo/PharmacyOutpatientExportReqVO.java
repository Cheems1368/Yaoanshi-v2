package com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 药学门诊 Excel 导出 Request VO，参数和 PharmacyOutpatientPageReqVO 是一致的")
@Data
public class PharmacyOutpatientExportReqVO {

    @Schema(description = "处方ID", example = "9390")
    private Long prescriptionId;

    @Schema(description = "问题描述", example = "王五")
    private String medicineName;

    @Schema(description = "用法用量")
    private String medicineUsage;

    @Schema(description = "开始用药时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startUsageTime;

    @Schema(description = "停止用药时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] stopUsageTime;

    @Schema(description = "停药原因", example = "不好")
    private String stopReason;

    @Schema(description = "mrp 类别", example = "1")
    private String mrpType;

    @Schema(description = "权重")
    private String weight;

    @Schema(description = "mrp 描述")
    private String mrpDesc;

    @Schema(description = "检查日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
