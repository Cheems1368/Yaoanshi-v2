package com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用药咨询 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MedicineConsultationBaseVO {

    @Schema(description = "患者ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15938")
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @Schema(description = "咨询内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "咨询内容不能为空")
    private String question;

    @Schema(description = "回答内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String answer;

    @Schema(description = "咨询对象(0 患者, 1 医护人员）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "咨询对象(0 患者, 1 医护人员）不能为空")
    private Byte consultObject;

    @Schema(description = "特殊人群 (0 否, 哺乳期 1, 妊娠期 2)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "特殊人群 (0 否, 哺乳期 1, 妊娠期 2)不能为空")
    private Byte specialPatient;

    @Schema(description = "咨询方式(0面对面 1电话 2 互联网）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Byte type;

    @Schema(description = "咨询时长", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer duration;

    @Schema(description = "咨询医师", requiredMode = Schema.RequiredMode.REQUIRED, example = "7271")
    @NotNull(message = "咨询医师不能为空")
    private String doctorId;

    @Schema(description = "回答依据")
    private String answerBasis;

    @Schema(description = "备注")
    private String comment;

    @Schema(description = "咨询药物", requiredMode = Schema.RequiredMode.REQUIRED, example = "24265")
    @NotNull(message = "咨询药物不能为空")
    private Long medicineId;

}
