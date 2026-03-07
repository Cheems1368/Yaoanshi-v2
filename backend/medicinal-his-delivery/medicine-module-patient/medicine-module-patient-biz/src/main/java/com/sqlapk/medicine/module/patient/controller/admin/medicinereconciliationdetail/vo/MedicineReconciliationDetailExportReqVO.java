package com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 药物重整剂量 Excel 导出 Request VO，参数和 MedicineReconciliationDetailPageReqVO 是一致的")
@Data
public class MedicineReconciliationDetailExportReqVO {

    @Schema(description = "重整ID", example = "32520")
    private Long reconciliationId;

    @Schema(description = "药品名称", example = "王五")
    private String medicineName;

    @Schema(description = "用法用量")
    private String dosage;

    @Schema(description = "用药原因", example = "不喜欢")
    private String usageReason;

    @Schema(description = "开始日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "访视时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "备注,重整原因")
    private String comment;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
