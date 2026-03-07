package com.sqlapk.medicine.module.medication.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 今日待办 DO
 *
 * @author sqlapk
 */
@TableName("med_today_tasks")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TodayTaskDO extends BaseDO {

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 计划项ID
     */
    private Long planItemId;
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 任务日期
     */
    private LocalDate taskDate;
    
    /**
     * 计划时间
     */
    private LocalTime scheduledTime;
    
    /**
     * 任务状态(0-待完成,1-已完成,2-已跳过)
     */
    private Integer status;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
    
    /**
     * 备注
     */
    private String notes;

}
