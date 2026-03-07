package com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 门诊药学检查项目明细创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OutpatientExamCreateReqVO extends OutpatientExamBaseVO {

}
