package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 提醒任务创建请求 DTO
 *
 * @author sqlapk
 */
@Data
public class ReminderTaskCreateReqDTO {

    /**
     * 今日待办任务ID
     */
    @NotNull(message = "今日待办任务ID不能为空")
    private Long todayTaskId;

    /**
     * 患者ID
     */
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    /**
     * 计划提醒时间
     */
    @NotNull(message = "计划提醒时间不能为空")
    private LocalDateTime scheduledTime;

    /**
     * 提醒类型(1-用药提醒,2-复诊提醒,3-检查提醒)
     */
    @NotNull(message = "提醒类型不能为空")
    private Integer type;

    /**
     * 通知内容
     */
    private String notificationContent;

}
