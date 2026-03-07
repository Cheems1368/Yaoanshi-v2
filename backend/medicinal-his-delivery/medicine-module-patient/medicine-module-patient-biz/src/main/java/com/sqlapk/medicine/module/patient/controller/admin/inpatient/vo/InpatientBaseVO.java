package com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 住院药学服务 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class InpatientBaseVO {

    @Schema(description = "住院号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "住院号不能为空")
    private String hospitalNumber;

    @Schema(description = "床号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "床号不能为空")
    private String bedNumber;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotNull(message = "姓名不能为空")
    private String name;

    @Schema(description = "性别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "性别不能为空")
    private String gender;

    @Schema(description = "住院日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "住院日期不能为空")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime admissionDate;

    @Schema(description = "主诉诊断")
    private String diagnosis;

    @Schema(description = "依从性评价")
    private String compliance;

    @Schema(description = "患者病情")
    private String patientCondition;

    @Schema(description = "特殊生理状态", example = "2")
    private String specialPhysiologicalStatus;

    @Schema(description = "调整方案")
    private String adjustmentPlan;

    private Long patientId;

    private Long doctorId;

}
