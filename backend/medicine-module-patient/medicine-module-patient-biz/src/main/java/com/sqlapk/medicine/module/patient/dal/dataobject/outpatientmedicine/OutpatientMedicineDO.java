package com.sqlapk.medicine.module.patient.dal.dataobject.outpatientmedicine;

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
 * 药学门诊 DO
 *
 * @author 管理员
 */
@TableName("patient_outpatient_medicine")
@KeySequence("patient_outpatient_medicine_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutpatientMedicineDO extends BaseDO {

    /**
     * 咨询ID
     */
    @TableId
    private Long id;
    /**
     * 门诊药学ID
     */
    private Long outpatientId;
    /**
     * 药学门诊ID
     */
    private Long meicineId;

}
