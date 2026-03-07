package com.sqlapk.medicine.module.system.api.dto.chat;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息响应 DTO
 *
 * @author sqlapk
 */
@Data
public class ChatMessageRespDTO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 工单ID
     */
    private Long orderId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者类型
     */
    private String senderType;

    /**
     * 发送者姓名
     */
    private String senderName;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 接收者类型
     */
    private String receiverType;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 语音文件URL
     */
    private String voiceUrl;

    /**
     * 语音时长（秒）
     */
    private Integer voiceDuration;

    /**
     * 语音转文字结果
     */
    private String voiceToText;

    /**
     * 附件URL列表
     */
    private List<String> attachments;

    /**
     * 消息状态
     */
    private String status;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 是否撤回
     */
    private Boolean isRecalled;

    /**
     * 撤回时间
     */
    private LocalDateTime recallTime;

    /**
     * 撤回原因
     */
    private String recallReason;
}
