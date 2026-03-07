package com.sqlapk.medicine.module.patient.controller.admin.patient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PatientsSource {

    @Schema(description = "患者ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13940")
    private Integer value;


    @Schema(description = "生日", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}
