package com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 药学门诊更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OutpatientMedicineUpdateReqVO extends OutpatientMedicineBaseVO {

    @Schema(description = "咨询ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23894")
    @NotNull(message = "咨询ID不能为空")
    private Long id;

}
