package com.sqlapk.medicine.module.patient.dal.dataobject.outpatientexam;

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
 * 门诊药学检查项目明细 DO
 *
 * @author 管理员
 */
@TableName("patient_outpatient_exam")
@KeySequence("patient_outpatient_exam_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutpatientExamDO extends BaseDO {

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
     * 检查项目ID
     */
    private Long examinationsId;

}
