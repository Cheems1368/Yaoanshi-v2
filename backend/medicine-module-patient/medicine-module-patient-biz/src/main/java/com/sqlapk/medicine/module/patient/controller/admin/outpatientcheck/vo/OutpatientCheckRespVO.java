package com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 处方审核 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OutpatientCheckRespVO extends OutpatientCheckBaseVO {

    @Schema(description = "审核ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19418")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
