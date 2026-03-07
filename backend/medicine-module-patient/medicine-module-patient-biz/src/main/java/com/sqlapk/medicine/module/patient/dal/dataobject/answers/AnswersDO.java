package com.sqlapk.medicine.module.patient.dal.dataobject.answers;

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
 * 问卷 DO
 *
 * @author 管理员
 */
@TableName("patient_answers")
@KeySequence("patient_answers_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswersDO extends BaseDO {

    /**
     * 答案ID
     */
    @TableId
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;

    private Long patientId;
    /**
     * 问题ID
     */
    private Long questionId;
    /**
     * 选项ID
     */
    private Long optionId;

    private String content;
    /**
     * 答案值
     */
    private Double value;

}
