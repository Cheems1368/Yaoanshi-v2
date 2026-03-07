package com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 主要用药咨询更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientConsultationUpdateReqVO extends InpatientConsultationBaseVO {

    @Schema(description = "咨询ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "4905")
    @NotNull(message = "咨询ID不能为空")
    private Long id;

}
