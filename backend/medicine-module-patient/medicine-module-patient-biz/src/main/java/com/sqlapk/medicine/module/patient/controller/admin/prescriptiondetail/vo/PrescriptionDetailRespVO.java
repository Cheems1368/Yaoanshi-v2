package com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 患者处方 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PrescriptionDetailRespVO extends PrescriptionDetailBaseVO {

    @Schema(description = "处方ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5081")
    private Long id;

    @Schema(description = "检查日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    private String name;
    private String medicineName;

    private Long drugId;

    private Long doctorId;

}
