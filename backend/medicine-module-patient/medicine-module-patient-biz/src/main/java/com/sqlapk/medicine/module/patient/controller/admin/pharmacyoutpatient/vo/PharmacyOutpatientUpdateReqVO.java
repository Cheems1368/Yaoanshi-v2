package com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 药学门诊更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PharmacyOutpatientUpdateReqVO extends PharmacyOutpatientBaseVO {

    @Schema(description = "门诊ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8017")
    @NotNull(message = "门诊ID不能为空")
    private Long id;

}
