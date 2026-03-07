package com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 门诊药学检查项目明细更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OutpatientExamUpdateReqVO extends OutpatientExamBaseVO {

    @Schema(description = "咨询ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31091")
    @NotNull(message = "咨询ID不能为空")
    private Long id;

}
