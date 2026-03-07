package com.sqlapk.medicine.module.patient.dal.dataobject.inpatient;

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
 * 住院药学服务 DO
 *
 * @author 管理员
 */
@TableName("patient_inpatient")
@KeySequence("patient_inpatient_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InpatientDO extends BaseDO {

    /**
     * 住院服务ID
     */
    @TableId
    private Long id;
    /**
     * 住院号
     */
    private String hospitalNumber;
    /**
     * 床号
     */
    private String bedNumber;
    /**
     * 姓名
     */
    private String name;
    /**
     * 性别
     */
    private String gender;
    /**
     * 住院日期
     */
    private LocalDateTime admissionDate;
    /**
     * 主诉诊断
     */
    private String diagnosis;
    /**
     * 依从性评价
     */
    private String compliance;
    /**
     * 患者病情
     */
    private String patientCondition;
    /**
     * 特殊生理状态
     */
    private String specialPhysiologicalStatus;
    /**
     * 调整方案
     */
    private String adjustmentPlan;

    private Long patientId;

    private Long doctorId;

}
