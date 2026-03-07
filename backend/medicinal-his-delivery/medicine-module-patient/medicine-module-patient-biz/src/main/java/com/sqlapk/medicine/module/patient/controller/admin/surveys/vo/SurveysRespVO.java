package com.sqlapk.medicine.module.patient.controller.admin.surveys.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 问卷 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SurveysRespVO extends SurveysBaseVO {

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23190")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
