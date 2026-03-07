package com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 处方审核更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OutpatientCheckUpdateReqVO extends OutpatientCheckBaseVO {

    @Schema(description = "审核ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19418")
    @NotNull(message = "审核ID不能为空")
    private Long id;

}
