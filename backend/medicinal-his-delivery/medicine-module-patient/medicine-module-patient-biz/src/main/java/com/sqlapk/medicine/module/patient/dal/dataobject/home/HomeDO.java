package com.sqlapk.medicine.module.patient.dal.dataobject.home;

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
 * 居家药学服务 DO
 *
 * @author 管理员
 */
@TableName("patient_home")
@KeySequence("patient_home_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeDO extends BaseDO {

    /**
     * 居家药学服务ID
     */
    @TableId
    private Long id;
    /**
     * 医生ID
     */
    private Long doctorId;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 患者姓名
     */
    private String name;
    /**
     * 医保卡号
     */
    private String insurance;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 创建时间
     */
    private LocalDateTime birthday;
    /**
     * 家庭住址
     */
    private String address;
    /**
     * 联系方式
     */
    private String phone;
    /**
     * 访视地点
     */
    private String interviewLocation;
    /**
     * 访视时间
     */
    private LocalDateTime interviewTime;
    /**
     * 合并疾病
     */
    private String complicatedDisease;
    /**
     * 访视目的
     */
    private String interviewPurpose;

}
