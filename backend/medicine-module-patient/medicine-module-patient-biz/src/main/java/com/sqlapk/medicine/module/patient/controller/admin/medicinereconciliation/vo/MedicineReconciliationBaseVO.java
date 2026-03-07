package com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 药物重整剂量 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MedicineReconciliationBaseVO {

    @Schema(description = "患者ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21317")
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @Schema(description = "医生ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5084")
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    @Schema(description = "患者姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotNull(message = "患者姓名不能为空")
    private String name;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime birthday;

    @Schema(description = "联系方式")
    private String phone;

    @Schema(description = "入院/就诊时间")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime clinicTime;

    @Schema(description = "出院/转科时间")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime dischargeTime;

    @Schema(description = "主要诊断")
    private String diagnose;

    @Schema(description = "过敏史")
    private String allergyHistory;

    @Schema(description = "用药相关问题")
    private String medicineQuestion;

    @Schema(description = "药师姓名")
    private String medicineDoctor;

}
