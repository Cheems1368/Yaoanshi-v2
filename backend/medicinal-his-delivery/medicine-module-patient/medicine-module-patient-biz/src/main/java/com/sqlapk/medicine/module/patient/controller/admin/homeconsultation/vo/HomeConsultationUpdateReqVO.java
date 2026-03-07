package com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 居家用药咨询更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HomeConsultationUpdateReqVO extends HomeConsultationBaseVO {

    @Schema(description = "咨询ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22378")
    @NotNull(message = "咨询ID不能为空")
    private Long id;

}
