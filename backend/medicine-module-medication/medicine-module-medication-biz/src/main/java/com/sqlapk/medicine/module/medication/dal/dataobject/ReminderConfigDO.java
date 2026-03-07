package com.sqlapk.medicine.module.medication.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 提醒配置 DO
 *
 * @author sqlapk
 */
@TableName("reminder_configs")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderConfigDO extends BaseDO {

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 提醒类型(1-用药提醒,2-复诊提醒,3-检查提醒)
     */
    private Integer type;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 提前分钟数
     */
    private Integer advanceMinutes;
    
    /**
     * 通知方式(SMS,EMAIL,SYSTEM,ALL)
     */
    private String notificationMethod;
    
    /**
     * 模板ID
     */
    private String templateId;

}
