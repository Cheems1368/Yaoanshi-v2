package com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 患者处方 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PrescriptionDetailBaseVO {

    @Schema(description = "处方ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "16623")
    @NotNull(message = "处方ID不能为空")
    private Long prescriptionId;

    @Schema(description = "药品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2377")
    @NotNull(message = "药品ID不能为空")
    private Long medicineId;

    @Schema(description = "用量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用量不能为空")
    private String dosage;

    @Schema(description = "用法")
    private String usageInfo;

    @Schema(description = "频次")
    private String frequency;

    @Schema(description = "数量")
    private Integer quality;

    @Schema(description = "单位")
    private String unit;

}
