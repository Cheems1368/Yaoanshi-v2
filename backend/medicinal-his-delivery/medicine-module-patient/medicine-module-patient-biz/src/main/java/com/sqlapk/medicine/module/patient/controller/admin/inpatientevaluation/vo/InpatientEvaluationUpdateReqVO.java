package com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 住院药学用药评估更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientEvaluationUpdateReqVO extends InpatientEvaluationBaseVO {

    @Schema(description = "记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2138")
    @NotNull(message = "记录ID不能为空")
    private Long id;

}
