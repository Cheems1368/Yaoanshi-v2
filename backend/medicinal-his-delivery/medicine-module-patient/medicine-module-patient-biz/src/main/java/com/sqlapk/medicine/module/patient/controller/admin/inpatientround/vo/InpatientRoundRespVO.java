package com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 住院药学查房记录 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientRoundRespVO extends InpatientRoundBaseVO {

    @Schema(description = "查房id", requiredMode = Schema.RequiredMode.REQUIRED, example = "26835")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
