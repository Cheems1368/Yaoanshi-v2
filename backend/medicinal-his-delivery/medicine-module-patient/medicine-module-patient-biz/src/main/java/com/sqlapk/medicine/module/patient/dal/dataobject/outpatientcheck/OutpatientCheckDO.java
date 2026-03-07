package com.sqlapk.medicine.module.patient.dal.dataobject.outpatientcheck;

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

/**
 * 处方审核 DO
 *
 * @author 管理员
 */
@TableName("patient_outpatient_check")
@KeySequence("patient_outpatient_check_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutpatientCheckDO extends BaseDO {

    /**
     * 审核ID
     */
    @TableId
    private Long id;
    /**
     * 门诊药学ID
     */
    private Long outpatientId;
    /**
     * 不合理用药名称
     */
    private String medicineName;
    /**
     * 处方医生
     */
    private Long doctorId;
    /**
     * 处方ID
     */
    private Long prescriptionId;
    /**
     * 问题描述
     */
    private String issueDesc;
    /**
     * 问题代码
     */
    private String issueCode;
    /**
     * 干预结果
     */
    private String interveneResult;

}
