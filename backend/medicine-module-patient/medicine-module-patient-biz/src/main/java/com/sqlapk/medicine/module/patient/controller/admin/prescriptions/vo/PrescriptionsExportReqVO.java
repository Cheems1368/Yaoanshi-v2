package com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 处方 Excel 导出 Request VO，参数和 PrescriptionsPageReqVO 是一致的")
@Data
public class PrescriptionsExportReqVO {

    @Schema(description = "患者ID", example = "593")
    private Long patientId;

    @Schema(description = "检查日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "处方类型(门诊,住院,居家,药学)", example = "2")
    private Integer type;

    @Schema(description = "医生", example = "31568")
    private Long doctorId;

    @Schema(description = "初步诊断")
    private String diagnosis;

}
