package com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 药物重整剂量分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MedicineReconciliationPageReqVO extends PageParam {

    @Schema(description = "患者ID", example = "21317")
    private Long patientId;

    @Schema(description = "医生ID", example = "5084")
    private Long doctorId;

    @Schema(description = "患者姓名", example = "赵六")
    private String name;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "创建时间")
    private LocalDateTime birthday;

    @Schema(description = "联系方式")
    private String phone;

    @Schema(description = "入院/就诊时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] clinicTime;

    @Schema(description = "出院/转科时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] dischargeTime;

    @Schema(description = "主要诊断")
    private String diagnose;

    @Schema(description = "过敏史")
    private String allergyHistory;

    @Schema(description = "用药相关问题")
    private String medicineQuestion;

    @Schema(description = "药师姓名")
    private String medicineDoctor;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
