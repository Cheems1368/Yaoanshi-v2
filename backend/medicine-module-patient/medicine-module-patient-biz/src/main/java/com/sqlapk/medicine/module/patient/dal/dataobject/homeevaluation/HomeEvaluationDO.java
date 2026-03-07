package com.sqlapk.medicine.module.patient.dal.dataobject.homeevaluation;

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
 * 居家随访评估 DO
 *
 * @author 管理员
 */
@TableName("patient_home_evaluation")
@KeySequence("patient_home_evaluation_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeEvaluationDO extends BaseDO {

    /**
     * 教育记录
     */
    @TableId
    private Long id;
    /**
     * 日期
     */
    private LocalDateTime evaluationDate;
    /**
     * 住院药学ID
     */
    private Long homeId;
    /**
     * 日期
     */
    private String symptom;
    /**
     * 检查指标
     */
    private String indicator;
    /**
     * 治疗问题
     */
    private String treatmentProblem;

}
