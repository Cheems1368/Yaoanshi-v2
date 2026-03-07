package com.sqlapk.medicine.module.patient.controller.admin.patient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 患者 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PatientsRespVO extends PatientsBaseVO {

    @Schema(description = "患者ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13940")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "生日", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime birthday;

}
