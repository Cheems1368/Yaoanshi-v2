package com.sqlapk.medicine.module.patient.dal.dataobject.surveys;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;

/**
 * 问卷 DO
 *
 * @author 管理员
 */
@TableName("patient_surveys")
@KeySequence("patient_surveys_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveysDO extends BaseDO {

    /**
     * 问卷ID
     */
    @TableId
    private Long id;
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String description;

}
