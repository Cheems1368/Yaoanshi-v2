package com.sqlapk.medicine.module.patient.dal.dataobject.questions;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;

/**
 * 问卷问题 DO
 *
 * @author 管理员
 */
@TableName("patient_questions")
@KeySequence("patient_questions_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionsDO extends BaseDO {

    /**
     * 问题ID
     */
    @TableId
    private Long id;
    /**
     * 问卷内容
     */
    private String content;
    /**
     * 问卷ID
     */
    private Long surveyId;

}
