package com.sqlapk.medicine.module.patient.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 患者信息 Response DTO
 *
 * @author sqlapk
 */
@Data
public class PatientsRespDTO {

    /**
     * 患者ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 患者姓名
     */
    private String name;

    /**
     * 患者性别（男(0)、女(1)）
     */
    private Integer gender;

    /**
     * 患者手机号
     */
    private String mobile;

    /**
     * 患者身份证号
     */
    private String idCard;

    /**
     * 患者出生年月
     */
    private LocalDateTime birthday;

    /**
     * 家庭住址
     */
    private String address;

    /**
     * 医保卡号
     */
    private String insuranceCardNumber;

    /**
     * 过敏史
     */
    private String allergyHistory;

    /**
     * 患病史
     */
    private String medicalHistory;

    /**
     * 家族史
     */
    private String familyHistory;

    /**
     * 个人史
     */
    private String personalHistory;

    /**
     * 患者类型（住院(0)、门诊(1)、居家(2)）
     */
    private Short patientType;

    /**
     * 报销类型（医保(0)、非医保(1)）
     */
    private Short insurance;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    private String emergencyPhone;

    /**
     * 审核状态（待审核(0)、已通过(1)、已拒绝(2)、需要修改(3)）
     */
    private Integer auditStatus;

    /**
     * 完整性得分
     */
    private Double completenessScore;

    /**
     * 完整性等级
     */
    private String completenessLevel;

}
