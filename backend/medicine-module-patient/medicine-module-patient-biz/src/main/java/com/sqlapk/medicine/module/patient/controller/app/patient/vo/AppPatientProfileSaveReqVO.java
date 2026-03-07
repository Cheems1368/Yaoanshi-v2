package com.sqlapk.medicine.module.patient.controller.app.patient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * App 端患者档案保存请求
 */
@Data
public class AppPatientProfileSaveReqVO {

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Schema(description = "证件号码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "证件号码不能为空")
    private String idNumber;

    @Schema(description = "证件类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "证件类型不能为空")
    private String idType;

    @Schema(description = "民族", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "民族不能为空")
    private String ethnicity;

    @Schema(description = "出生日期(yyyy-MM-dd)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "出生日期不能为空")
    private String birthday;

    @Schema(description = "患者类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "患者类型不能为空")
    private Short patientType;

    @Schema(description = "报销类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "报销类型不能为空")
    private Short insurance;

    @Schema(description = "家庭住址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "家庭住址不能为空")
    private String address;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    private String mobile;

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

    @Schema(description = "过敏信息")
    private String allergies;

    @Schema(description = "血型")
    private String bloodType;

    @Schema(description = "身高(cm)")
    private Integer height;

    @Schema(description = "体重(kg)")
    private Integer weight;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "性别(0 男 1 女)")
    private Integer gender;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "验证码(前端校验使用)")
    private String verificationCode;
}
