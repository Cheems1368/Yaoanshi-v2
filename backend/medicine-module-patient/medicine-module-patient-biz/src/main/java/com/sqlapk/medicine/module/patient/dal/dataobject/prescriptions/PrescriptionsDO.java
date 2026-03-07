package com.sqlapk.medicine.module.patient.dal.dataobject.prescriptions;

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
 * 处方 DO
 *
 * @author 管理员
 */
@TableName("patient_prescriptions")
@KeySequence("patient_prescriptions_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionsDO extends BaseDO {

    /**
     * 处方ID
     */
    @TableId
    private Long id;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 处方类型(门诊,住院,居家,药学)
     */
    private Integer type;
    /**
     * 医生
     */
    private Long doctorId;
    /**
     * 初步诊断
     */
    private String diagnosis;

}
