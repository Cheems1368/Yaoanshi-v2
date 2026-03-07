package com.sqlapk.medicine.module.patient.controller.app.patient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * App 端患者档案响应
 */
@Data
public class AppPatientProfileRespVO {

    @Schema(description = "患者 ID")
    private Long patientId;

    @Schema(description = "系统用户 ID")
    private Long userId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "手机号(兼容字段)")
    private String phone;

    @Schema(description = "证件号码")
    private String idNumber;

    @Schema(description = "证件类型")
    private String idType;

    @Schema(description = "民族")
    private String ethnicity;

    @Schema(description = "出生日期")
    private String birthday;

    @Schema(description = "患者类型")
    private Short patientType;

    @Schema(description = "报销类型")
    private Short insurance;

    @Schema(description = "家庭住址")
    private String address;

    @Schema(description = "是否实名认证")
    private Boolean realNameVerified;

    @Schema(description = "实名认证时间")
    private LocalDateTime realNameVerifiedTime;

    @Schema(description = "过敏史")
    private String allergyHistory;

    @Schema(description = "患病史")
    private String medicalHistory;

    @Schema(description = "家族史")
    private String familyHistory;

    @Schema(description = "个人史")
    private String personalHistory;

    @Schema(description = "医保卡号")
    private String insuranceCardNumber;

    @Schema(description = "紧急联系人")
    private String emergencyContact;

    @Schema(description = "紧急联系电话")
    private String emergencyContactPhone;

    @Schema(description = "紧急联系人关系")
    private String emergencyContactRelationship;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "身高(cm)")
    private Integer height;

    @Schema(description = "体重(kg)")
    private Integer weight;

    @Schema(description = "血型")
    private String bloodType;

    @Schema(description = "过敏信息")
    private String allergies;

    @Schema(description = "头像地址")
    private String avatar;
}
