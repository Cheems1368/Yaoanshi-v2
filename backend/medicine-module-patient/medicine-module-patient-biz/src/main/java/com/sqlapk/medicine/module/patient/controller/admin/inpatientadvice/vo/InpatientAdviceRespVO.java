package com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 住院药学不合理用药 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientAdviceRespVO extends InpatientAdviceBaseVO {

    @Schema(description = "住院医嘱ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23552")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
