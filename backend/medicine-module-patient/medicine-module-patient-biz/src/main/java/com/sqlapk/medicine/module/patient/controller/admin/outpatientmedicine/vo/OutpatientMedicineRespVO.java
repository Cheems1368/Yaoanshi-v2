package com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 药学门诊 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OutpatientMedicineRespVO extends OutpatientMedicineBaseVO {

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "咨询ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23894")
    private Long id;

}
