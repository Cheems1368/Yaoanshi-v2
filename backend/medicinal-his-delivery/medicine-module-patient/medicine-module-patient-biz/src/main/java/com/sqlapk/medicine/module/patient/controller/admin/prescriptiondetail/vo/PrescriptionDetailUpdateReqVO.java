package com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 患者处方更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PrescriptionDetailUpdateReqVO extends PrescriptionDetailBaseVO {

    @Schema(description = "处方ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5081")
    @NotNull(message = "处方ID不能为空")
    private Long id;

}
