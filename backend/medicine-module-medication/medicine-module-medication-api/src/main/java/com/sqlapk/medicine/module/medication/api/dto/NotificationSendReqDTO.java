package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 通知发送请求 DTO
 *
 * @author sqlapk
 */
@Data
public class NotificationSendReqDTO {

    /**
     * 患者ID
     */
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    /**
     * 通知标题
     */
    @NotBlank(message = "通知标题不能为空")
    private String title;

    /**
     * 通知内容
     */
    @NotBlank(message = "通知内容不能为空")
    private String content;

    /**
     * 通知类型(1-用药提醒,2-计划变更,3-异常提醒,4-报告生成)
     */
    @NotNull(message = "通知类型不能为空")
    private Integer type;

    /**
     * 通知渠道(1-站内,2-短信,3-语音,4-邮件)
     */
    private Integer channel = 1; // 默认站内通知

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

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
     * 扩展信息
     */
    private String extInfo;

}
