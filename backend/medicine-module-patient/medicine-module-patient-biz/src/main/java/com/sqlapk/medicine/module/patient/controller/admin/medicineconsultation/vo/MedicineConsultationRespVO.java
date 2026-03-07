package com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 用药咨询 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MedicineConsultationRespVO extends MedicineConsultationBaseVO {

    @Schema(description = "咨询ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3914")
    private Long id;

    @Schema(description = "咨询日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    private String medicineName;
    private String doctorName;
    private String patientName;

}
