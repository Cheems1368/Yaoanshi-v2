package com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 用药咨询更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MedicineConsultationUpdateReqVO extends MedicineConsultationBaseVO {

    @Schema(description = "咨询ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3914")
    @NotNull(message = "咨询ID不能为空")
    private Long id;

}
