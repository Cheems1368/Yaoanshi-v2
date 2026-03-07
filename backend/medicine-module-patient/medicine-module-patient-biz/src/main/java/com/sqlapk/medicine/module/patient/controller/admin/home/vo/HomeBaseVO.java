package com.sqlapk.medicine.module.patient.controller.admin.home.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static com.sqlapk.medicine.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

/**
 * 居家药学服务 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class HomeBaseVO {

    @Schema(description = "医生ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8889")
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    @Schema(description = "患者ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "24709")
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @Schema(description = "患者姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotNull(message = "患者姓名不能为空")
    private String name;

    @Schema(description = "医保卡号")
    private String insurance;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "创建时间")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime birthday;

    @Schema(description = "家庭住址")
    private String address;

    @Schema(description = "联系方式")
    private String phone;

    @Schema(description = "访视地点")
    private String interviewLocation;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime interviewTime;

    @Schema(description = "合并疾病")
    private String complicatedDisease;

    @Schema(description = "访视目的")
    private String interviewPurpose;

}
