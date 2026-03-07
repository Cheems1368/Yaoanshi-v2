package com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用药咨询 Excel 导出 Request VO，参数和 MedicineConsultationPageReqVO 是一致的")
@Data
public class MedicineConsultationExportReqVO {

    @Schema(description = "患者ID", example = "15938")
    private Long patientId;

    @Schema(description = "咨询内容")
    private String question;

    @Schema(description = "回答内容")
    private String answer;

    @Schema(description = "咨询对象(0 患者, 1 医护人员）")
    private Byte consultObject;

    @Schema(description = "特殊人群 (0 否, 哺乳期 1, 妊娠期 2)")
    private Byte specialPatient;

    @Schema(description = "咨询方式(0面对面 1电话 2 互联网）", example = "2")
    private Byte type;

    @Schema(description = "咨询时长")
    private Integer duration;

    @Schema(description = "咨询医师", example = "7271")
    private String doctorId;

    @Schema(description = "回答依据")
    private String answerBasis;

    @Schema(description = "备注")
    private String comment;

    @Schema(description = "咨询日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "咨询药物", example = "24265")
    private Long medicineId;

}
