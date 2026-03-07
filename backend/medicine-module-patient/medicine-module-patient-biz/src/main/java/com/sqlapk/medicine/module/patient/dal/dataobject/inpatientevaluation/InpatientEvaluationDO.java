package com.sqlapk.medicine.module.patient.dal.dataobject.inpatientevaluation;

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
 * 住院药学用药评估 DO
 *
 * @author 管理员
 */
@TableName("patient_inpatient_evaluation")
@KeySequence("patient_inpatient_evaluation_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InpatientEvaluationDO extends BaseDO {

    /**
     * 记录ID
     */
    @TableId
    private Long id;
    /**
     * 住院药学ID
     */
    private Long inpatientId;
    /**
     * 日期
     */
    private LocalDateTime evaluationDate;
    /**
     * 目标药物
     */
    private String targetDrug;
    /**
     * 症状
     */
    private String symptom;
    /**
     * 结果
     */
    private String result;
    /**
     * 用药建议
     */
    private String medicationSuggestion;

}
