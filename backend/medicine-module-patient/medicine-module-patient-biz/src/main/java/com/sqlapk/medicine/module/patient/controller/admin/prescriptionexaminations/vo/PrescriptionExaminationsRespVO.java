package com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 处方检查项目关系 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PrescriptionExaminationsRespVO extends PrescriptionExaminationsBaseVO {

    @Schema(description = "主键", example = "1892")
    private Long id;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    private String description;

    private String name;

}
