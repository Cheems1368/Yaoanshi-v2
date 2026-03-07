package com.sqlapk.medicine.module.patient.controller.admin.options.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 问题选项 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OptionsRespVO extends OptionsBaseVO {

    @Schema(description = "选项ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1532")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
