package com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 居家随访评估更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HomeEvaluationUpdateReqVO extends HomeEvaluationBaseVO {

    @Schema(description = "教育记录", requiredMode = Schema.RequiredMode.REQUIRED, example = "15311")
    @NotNull(message = "教育记录不能为空")
    private Long id;

}
