package com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 居家用药教育 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HomeEducationRespVO extends HomeEducationBaseVO {

    @Schema(description = "教育记录", requiredMode = Schema.RequiredMode.REQUIRED, example = "22980")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
