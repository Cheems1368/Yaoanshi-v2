package com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 处方审核 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class OutpatientCheckBaseVO {

    @Schema(description = "门诊药学ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1346")
    @NotNull(message = "门诊药学ID不能为空")
    private Long outpatientId;

    @Schema(description = "不合理用药名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotNull(message = "不合理用药名称不能为空")
    private String medicineName;

    @Schema(description = "处方医生", example = "26050")
    private Long doctorId;

    @Schema(description = "处方ID", example = "27266")
    private Long prescriptionId;

    @Schema(description = "问题描述")
    private String issueDesc;

    @Schema(description = "问题代码")
    private String issueCode;

    @Schema(description = "干预结果")
    private String interveneResult;

}
