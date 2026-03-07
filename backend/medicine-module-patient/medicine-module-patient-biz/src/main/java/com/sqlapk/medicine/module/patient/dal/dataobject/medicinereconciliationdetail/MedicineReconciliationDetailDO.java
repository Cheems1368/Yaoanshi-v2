package com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliationdetail;

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
@TableName("patient_medicine_reconciliation_detail")
@KeySequence("patient_medicine_reconciliation_detail_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineReconciliationDetailDO extends BaseDO {

    /**
     * 明细ID
     */
    @TableId
    private Long id;
    /**
     * 重整ID
     */
    private Long reconciliationId;
    /**
     * 药品名称
     */
    private String medicineName;
    /**
     * 用法用量
     */
    private String dosage;
    /**
     * 用药原因
     */
    private String usageReason;
    /**
     * 开始日期
     */
    private LocalDateTime startTime;
    /**
     * 访视时间
     */
    private LocalDateTime endTime;
    /**
     * 备注,重整原因
     */
    private String comment;

}
