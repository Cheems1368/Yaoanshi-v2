package com.sqlapk.medicine.module.patient.controller.admin.home.vo;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 居家药学服务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HomePageReqVO extends PageParam {

    @Schema(description = "医生ID", example = "8889")
    private Long doctorId;

    @Schema(description = "患者ID", example = "24709")
    private Long patientId;

    @Schema(description = "患者姓名", example = "王五")
    private String name;

    @Schema(description = "医保卡号")
    private String insurance;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "创建时间")
    private LocalDateTime birthday;

    @Schema(description = "家庭住址")
    private String address;

    @Schema(description = "联系方式")
    private String phone;

    @Schema(description = "访视地点")
    private String interviewLocation;

    @Schema(description = "访视时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] interviewTime;

    @Schema(description = "合并疾病")
    private String complicatedDisease;

    @Schema(description = "访视目的")
    private String interviewPurpose;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
