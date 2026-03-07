package com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 药学门诊 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PharmacyOutpatientRespVO extends PharmacyOutpatientBaseVO {

    @Schema(description = "门诊ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8017")
    private Long id;

    @Schema(description = "检查日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
