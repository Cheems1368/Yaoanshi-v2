package com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 住院药学不合理用药更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientAdviceUpdateReqVO extends InpatientAdviceBaseVO {

    @Schema(description = "住院医嘱ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23552")
    @NotNull(message = "住院医嘱ID不能为空")
    private Long id;

}
