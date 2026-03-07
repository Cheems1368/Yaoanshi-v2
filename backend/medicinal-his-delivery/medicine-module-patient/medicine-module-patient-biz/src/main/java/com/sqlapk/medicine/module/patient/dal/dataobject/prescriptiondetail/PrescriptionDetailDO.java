package com.sqlapk.medicine.module.patient.dal.dataobject.prescriptiondetail;

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
 * 患者处方 DO
 *
 * @author 管理员
 */
@TableName("patient_prescription_detail")
@KeySequence("patient_prescription_detail_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDetailDO extends BaseDO {

    /**
     * 处方ID
     */
    @TableId
    private Long id;
    /**
     * 处方ID
     */
    private Long prescriptionId;
    /**
     * 药品ID
     */
    private Long medicineId;
    /**
     * 用量
     */
    private String dosage;
    /**
     * 用法
     */
    private String usageInfo;
    /**
     * 频次
     */
    private String frequency;
    /**
     * 数量
     */
    private Integer quality;
    /**
     * 单位
     */
    private String unit;

}
