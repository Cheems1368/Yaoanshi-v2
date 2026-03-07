package com.sqlapk.medicine.module.system.api.dto.chat;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 聊天消息请求 DTO
 *
 * @author sqlapk
 */
@Data
public class ChatMessageReqDTO {

    /**
     * 工单ID
     */
    @NotNull(message = "工单ID不能为空")
    private Long orderId;

    /**
     * 接收者ID
     */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /**
     * 接收者类型（patient/pharmacist/doctor）
     */
    @NotBlank(message = "接收者类型不能为空")
    private String receiverType;

    /**
     * 消息类型（text/voice/image/file）
     */
    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 语音文件URL（语音消息时使用）
     */
    private String voiceUrl;

    /**
     * 语音时长（秒）
     */
    private Integer voiceDuration;

    /**
     * 附件URL列表
     */
    private List<String> attachments;
}
