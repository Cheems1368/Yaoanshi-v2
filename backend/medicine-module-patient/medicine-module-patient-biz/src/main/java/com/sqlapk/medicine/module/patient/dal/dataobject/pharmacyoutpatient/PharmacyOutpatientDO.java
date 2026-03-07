package com.sqlapk.medicine.module.patient.dal.dataobject.pharmacyoutpatient;

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
 * 药学门诊 DO
 *
 * @author 管理员
 */
@TableName("patient_pharmacy_outpatient")
@KeySequence("patient_pharmacy_outpatient_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyOutpatientDO extends BaseDO {

    /**
     * 门诊ID
     */
    @TableId
    private Long id;
    /**
     * 处方ID
     */
    private Long prescriptionId;

    private Long patientId;
    /**
     * 问题描述
     */
    private String medicineName;
    /**
     * 用法用量
     */
    private String medicineUsage;
    /**
     * 开始用药时间
     */
    private LocalDateTime startUsageTime;
    /**
     * 停止用药时间
     */
    private LocalDateTime stopUsageTime;
    /**
     * 停药原因
     */
    private String stopReason;
    /**
     * mrp 类别
     */
    private String mrpType;
    /**
     * 权重
     */
    private String weight;
    /**
     * mrp 描述
     */
    private String mrpDesc;

}
