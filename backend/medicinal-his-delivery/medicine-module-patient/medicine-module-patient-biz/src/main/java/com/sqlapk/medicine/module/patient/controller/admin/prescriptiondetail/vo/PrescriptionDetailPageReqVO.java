package com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 患者处方分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PrescriptionDetailPageReqVO extends PageParam {

    @Schema(description = "处方ID", example = "16623")
    private Long prescriptionId;

    @Schema(description = "药品ID", example = "2377")
    private Long medicineId;

    @Schema(description = "用量")
    private String dosage;

    @Schema(description = "用法")
    private String usageInfo;

    @Schema(description = "频次")
    private String frequency;

    @Schema(description = "数量")
    private Integer quality;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "检查日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
