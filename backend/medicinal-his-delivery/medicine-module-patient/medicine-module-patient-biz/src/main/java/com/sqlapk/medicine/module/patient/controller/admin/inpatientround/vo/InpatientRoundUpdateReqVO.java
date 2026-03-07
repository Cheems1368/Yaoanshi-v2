package com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 住院药学查房记录更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientRoundUpdateReqVO extends InpatientRoundBaseVO {

    @Schema(description = "查房id", requiredMode = Schema.RequiredMode.REQUIRED, example = "26835")
    @NotNull(message = "查房id不能为空")
    private Long id;

}
