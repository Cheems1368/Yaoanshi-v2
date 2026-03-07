package com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 住院药学用药评估创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientEvaluationCreateReqVO extends InpatientEvaluationBaseVO {

}
