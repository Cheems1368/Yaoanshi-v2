package com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 药学门诊 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PharmacyOutpatientBaseVO {

    @Schema(description = "处方ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "9390")
    @NotNull(message = "处方ID不能为空")
    private Long prescriptionId;

    private Long patientId;

    @Schema(description = "问题描述", example = "王五")
    private String medicineName;

    @Schema(description = "用法用量")
    private String medicineUsage;

    @Schema(description = "开始用药时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startUsageTime;

    @Schema(description = "停止用药时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime stopUsageTime;

    @Schema(description = "停药原因", example = "不好")
    private String stopReason;

    @Schema(description = "mrp 类别", example = "1")
    private String mrpType;

    @Schema(description = "权重")
    private String weight;

    @Schema(description = "mrp 描述")
    private String mrpDesc;

}
