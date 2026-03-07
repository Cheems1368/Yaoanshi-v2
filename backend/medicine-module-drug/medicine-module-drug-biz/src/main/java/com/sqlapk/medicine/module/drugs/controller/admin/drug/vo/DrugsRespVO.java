package com.sqlapk.medicine.module.drugs.controller.admin.drug.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 药品 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DrugsRespVO extends DrugsBaseVO {

    @Schema(description = "药品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "429")
    private Long drugId;

}
