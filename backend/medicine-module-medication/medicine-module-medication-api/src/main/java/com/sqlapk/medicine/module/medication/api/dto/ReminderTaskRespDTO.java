package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提醒任务响应 DTO
 *
 * @author sqlapk
 */
@Data
public class ReminderTaskRespDTO {

    /**
     * 提醒任务ID
     */
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
     * 提醒类型名称
     */
    private String typeName;

    /**
     * 提醒状态(0-待执行,1-执行中,2-成功,3-失败,4-已放弃)
     */
    private Integer status;

    /**
     * 提醒状态名称
     */
    private String statusName;

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
