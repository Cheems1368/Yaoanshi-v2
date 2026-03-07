package com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 门诊药学服务 Excel 导出 Request VO，参数和 OutpatientPageReqVO 是一致的")
@Data
public class OutpatientExportReqVO {

    @Schema(description = "医生ID", example = "22876")
    private Long doctorId;

    @Schema(description = "患者ID", example = "17296")
    private Long patientId;

    @Schema(description = "过敏史")
    private String allergyHistory;

    @Schema(description = "患病史")
    private String medicalHistory;

    @Schema(description = "家族史")
    private String familyHistory;

    @Schema(description = "个人史")
    private String personalHistory;

    @Schema(description = "备注")
    private String comment;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
