package com.sqlapk.medicine.module.patient.dal.dataobject.options;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;

/**
 * 问题选项 DO
 *
 * @author 管理员
 */
@TableName("patient_options")
@KeySequence("patient_options_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionsDO extends BaseDO {

    /**
     * 选项ID
     */
    @TableId
    private Long id;
    /**
     * 选项内容
     */
    private String content;
    /**
     * 选项值
     */
    private Integer value;
    /**
     * 选项分值
     */
    private Integer score;
    /**
     * 问题ID
     */
    private Long questionId;

}
