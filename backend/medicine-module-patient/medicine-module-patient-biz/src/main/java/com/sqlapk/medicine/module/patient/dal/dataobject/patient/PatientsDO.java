package com.sqlapk.medicine.module.patient.dal.dataobject.patient;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 患者 DO
 */
@TableName("patients")
@KeySequence("patients_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientsDO extends BaseDO {

    /**
     * 患者 ID
     */
    @TableId
    private Long id;
    /**
     * 系统用户 ID
     */
    private Long userId;
    /**
     * 患者姓名
     */
    private String name;
    /**
     * 性别（男 0、女 1）
     */
    private Integer gender;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 联系手机号
     */
    private String mobile;
    /**
     * 身份证号码
     */
    private String idCard;
    /**
     * 证件类型
     */
    private String idType;
    /**
     * 民族
     */
    private String ethnicity;
    /**
     * 身高（cm）
     */
    private Integer height;
    /**
     * 体重（kg）
     */
    private Integer weight;
    /**
     * 血型
     */
    private String bloodType;
    /**
     * 过敏信息
     */
    private String allergies;
    /**
     * 头像地址
     */
    private String avatar;
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
     * 患者类型（住院 0、门诊 1、居家 2）
     */
    private Short patientType;
    /**
     * 报销类型（医保 0、非医保 1）
     */
    private Short insurance;
    /**
     * 出生日期
     */
    private LocalDateTime birthday;
    /**
     * 紧急联系人姓名
     */
    private String emergencyContact;
    /**
     * 紧急联系人电话
     */
    private String emergencyPhone;
    /**
     * 紧急联系人关系
     */
    private String emergencyContactRelationship;
    /**
     * 审核状态（待审核 0、已通过 1、已拒绝 2、需要修改 3）
     */
    private Integer auditStatus;
    /**
     * 档案完整性得分
     */
    private Double completenessScore;
    /**
     * 档案完整性等级
     */
    private String completenessLevel;
    /**
     * 是否通过实名认证
     */
    private Boolean isRealNameVerified;
    /**
     * 实名认证时间
     */
    private LocalDateTime realNameVerifiedTime;
}
