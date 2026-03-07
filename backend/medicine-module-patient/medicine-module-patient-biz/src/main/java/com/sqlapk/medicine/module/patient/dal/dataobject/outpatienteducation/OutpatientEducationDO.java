package com.sqlapk.medicine.module.patient.dal.dataobject.outpatienteducation;

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
 * 门诊处方用药教育明细 DO
 *
 * @author 管理员
 */
@TableName("patient_outpatient_education")
@KeySequence("patient_outpatient_education_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutpatientEducationDO extends BaseDO {

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
     * 教育ID
     */
    private Long educationId;

}
