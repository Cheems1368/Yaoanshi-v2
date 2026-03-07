package com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 居家随访评估 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HomeEvaluationRespVO extends HomeEvaluationBaseVO {

    @Schema(description = "教育记录", requiredMode = Schema.RequiredMode.REQUIRED, example = "15311")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
