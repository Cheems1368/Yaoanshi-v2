package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知响应 DTO
 *
 * @author sqlapk
 */
@Data
public class NotificationRespDTO {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型(1-用药提醒,2-计划变更,3-异常提醒,4-报告生成)
     */
    private Integer type;

    /**
     * 通知类型名称
     */
    private String typeName;

    /**
     * 通知状态(0-未读,1-已读,2-已删除)
     */
    private Integer status;

    /**
     * 通知状态名称
     */
    private String statusName;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 读取时间
     */
    private LocalDateTime readTime;

    /**
     * 关联的待办任务ID
     */
    private Long todoId;

    /**
     * 关联的计划ID
     */
    private Long planId;

    /**
     * 关联的报告ID
     */
    private Long reportId;

    /**
     * 通知渠道(1-站内,2-短信,3-语音,4-邮件)
     */
    private Integer channel;

    /**
     * 通知渠道名称
     */
    private String channelName;

    /**
     * 发送状态(0-待发送,1-发送中,2-发送成功,3-发送失败)
     */
    private Integer sendStatus;

    /**
     * 发送状态名称
     */
    private String sendStatusName;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 扩展信息
     */
    private String extInfo;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
