package com.sqlapk.medicine.module.patient.controller.admin.examinations.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 检查项目 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExaminationsRespVO extends ExaminationsBaseVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "24915")
    private Long id;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;

}
