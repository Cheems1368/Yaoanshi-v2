package com.sqlapk.medicine.module.medication.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 提醒任务 DO
 *
 * @author sqlapk
 */
@TableName("reminder_tasks")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderTaskDO extends BaseDO {

    /**
     * 提醒任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 今日待办任务ID
     */
    private Long todayTaskId;
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 计划提醒时间
     */
    private LocalDateTime scheduledTime;
    
    /**
     * 提醒类型(1-用药提醒,2-复诊提醒,3-检查提醒)
     */
    private Integer type;
    
    /**
     * 提醒状态(0-待执行,1-执行中,2-成功,3-失败,4-已放弃)
     */
    private Integer status;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 最后重试时间
     */
    private LocalDateTime lastRetryTime;
    
    /**
     * 通知内容
     */
    private String notificationContent;
    
    /**
     * 失败原因
     */
    private String failureReason;

}
