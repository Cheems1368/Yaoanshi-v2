package com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 住院药学服务 Excel 导出 Request VO，参数和 InpatientPageReqVO 是一致的")
@Data
public class InpatientExportReqVO {

    @Schema(description = "住院号")
    private String hospitalNumber;

    @Schema(description = "床号")
    private String bedNumber;

    @Schema(description = "姓名", example = "芋艿")
    private String name;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "住院日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] admissionDate;

    @Schema(description = "主诉诊断")
    private String diagnosis;

    @Schema(description = "依从性评价")
    private String compliance;

    @Schema(description = "患者病情")
    private String patientCondition;

    @Schema(description = "特殊生理状态", example = "2")
    private String specialPhysiologicalStatus;

    @Schema(description = "调整方案")
    private String adjustmentPlan;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    private Long patientId;

    private Long doctorId;

}
