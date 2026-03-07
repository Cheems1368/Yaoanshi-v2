package com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 住院用药教育 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientEducationRespVO extends InpatientEducationBaseVO {

    @Schema(description = "教育记录", requiredMode = Schema.RequiredMode.REQUIRED, example = "4326")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
