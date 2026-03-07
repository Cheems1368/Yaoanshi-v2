package com.sqlapk.medicine.module.drugs.controller.admin.drug.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 药品更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DrugsUpdateReqVO extends DrugsBaseVO {

    @Schema(description = "药品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "429")
    @NotNull(message = "药品ID不能为空")
    private Long drugId;

}
