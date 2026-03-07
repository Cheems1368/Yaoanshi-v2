package com.sqlapk.medicine.module.patient.controller.admin.questions.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 问卷问题 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuestionsRespVO extends QuestionsBaseVO {

    @Schema(description = "问题ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28039")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
