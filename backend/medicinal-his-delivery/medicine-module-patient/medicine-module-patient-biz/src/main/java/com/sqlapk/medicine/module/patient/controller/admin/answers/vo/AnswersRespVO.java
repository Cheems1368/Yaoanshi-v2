package com.sqlapk.medicine.module.patient.controller.admin.answers.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 问卷 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AnswersRespVO extends AnswersBaseVO {

    @Schema(description = "答案ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7671")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    private String patientName;

    private String questionName;
}
