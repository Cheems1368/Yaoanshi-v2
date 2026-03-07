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
 *
 * @author 管理员
 */
@TableName("patients")
@KeySequence("patients_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientsDO extends BaseDO {

    /**
     * 患者ID
     */
    @TableId
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
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
     *
     * 枚举 {@link TODO patient_type 对应的类}
     */
    private Short patientType;
    /**
     * 报销类型（医保(0)、非医保(1)）
     *
     * 枚举 {@link TODO insurance 对应的类}
     */
    private Short insurance;

    /**
     * 患者出生年月
     */
    private LocalDateTime birthday;

}
