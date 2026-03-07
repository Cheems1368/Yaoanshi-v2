package com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 处方 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PrescriptionsRespVO extends PrescriptionsBaseVO {

    @Schema(description = "处方ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22100")
    private Long id;

    @Schema(description = "检查日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
