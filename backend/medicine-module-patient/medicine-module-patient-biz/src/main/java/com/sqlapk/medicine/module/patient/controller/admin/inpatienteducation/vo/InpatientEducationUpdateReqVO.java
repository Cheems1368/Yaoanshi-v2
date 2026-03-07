package com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 住院用药教育更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientEducationUpdateReqVO extends InpatientEducationBaseVO {

    @Schema(description = "教育记录", requiredMode = Schema.RequiredMode.REQUIRED, example = "4326")
    @NotNull(message = "教育记录不能为空")
    private Long id;

}
