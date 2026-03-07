package com.sqlapk.medicine.module.patient.controller.admin.patient.vo;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 患者分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PatientsPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "26644")
    private Long userId;

    @Schema(description = "家庭住址")
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

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "患者类型（住院(0)、门诊(1)、居家(2)）", example = "1")
    private Short patientType;

    @Schema(description = "报销类型（医保(0)、非医保(1)）")
    private Short insurance;

}
