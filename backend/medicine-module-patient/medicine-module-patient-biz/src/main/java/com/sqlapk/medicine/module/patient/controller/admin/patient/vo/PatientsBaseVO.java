package com.sqlapk.medicine.module.patient.controller.admin.patient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 患者 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PatientsBaseVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26644")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "家庭住址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "家庭住址不能为空")
    private String address;

    @Schema(description = "医保卡号")
    private String insuranceCardNumber;

    @Schema(description = "过敏史")
    private String allergyHistory;

    @Schema(description = "患病史")
    private String medicalHistory;

    @Schema(description = "家族史")
    private String familyHistory;

    @Schema(description = "个人史")
    private String personalHistory;

    @Schema(description = "患者类型（住院(0)、门诊(1)、居家(2)）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "患者类型（住院(0)、门诊(1)、居家(2)）不能为空")
    private Short patientType;

    @Schema(description = "报销类型（医保(0)、非医保(1)）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "报销类型（医保(0)、非医保(1)）不能为空")
    private Short insurance;

    @Schema(description = "生日", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "生日")
    private LocalDateTime birthday;

}
