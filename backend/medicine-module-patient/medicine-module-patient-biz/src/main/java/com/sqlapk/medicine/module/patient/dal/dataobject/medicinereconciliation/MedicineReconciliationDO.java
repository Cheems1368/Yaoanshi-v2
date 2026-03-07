package com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliation;

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
 * 药物重整剂量 DO
 *
 * @author 管理员
 */
@TableName("patient_medicine_reconciliation")
@KeySequence("patient_medicine_reconciliation_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineReconciliationDO extends BaseDO {

    /**
     * 重整ID
     */
    @TableId
    private Long id;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 医生ID
     */
    private Long doctorId;
    /**
     * 患者姓名
     */
    private String name;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 创建时间
     */
    private LocalDateTime birthday;
    /**
     * 联系方式
     */
    private String phone;
    /**
     * 入院/就诊时间
     */
    private LocalDateTime clinicTime;
    /**
     * 出院/转科时间
     */
    private LocalDateTime dischargeTime;
    /**
     * 主要诊断
     */
    private String diagnose;
    /**
     * 过敏史
     */
    private String allergyHistory;
    /**
     * 用药相关问题
     */
    private String medicineQuestion;
    /**
     * 药师姓名
     */
    private String medicineDoctor;

}
