package com.sqlapk.medicine.module.system.api;

import com.sqlapk.medicine.module.system.api.dto.chat.ChatMessageReqDTO;
import com.sqlapk.medicine.module.system.api.dto.chat.ChatMessageRespDTO;

import java.util.List;

/**
 * 聊天服务 API 接口
 *
 * @author sqlapk
 */
public interface ChatServiceApi {

    /**
     * 发送聊天消息
     *
     * @param reqDTO 消息请求
     * @param senderId 发送者ID
     * @param senderType 发送者类型
     * @return 消息ID
     */
    Long sendMessage(ChatMessageReqDTO reqDTO, Long senderId, String senderType);

    /**
     * 获取工单聊天历史
     *
     * @param orderId 工单ID
     * @return 聊天消息列表
     */
    List<ChatMessageRespDTO> getChatHistory(Long orderId);

    /**
     * 获取用户未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Long getUnreadMessageCount(Long userId);

    /**
     * 标记消息为已读
     *
     * @param userId 用户ID
     * @param orderId 工单ID
     * @return 标记成功的消息数量
     */
    Integer markMessagesAsRead(Long userId, Long orderId);

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param reason 撤回原因
     * @return 是否成功
     */
    Boolean recallMessage(Long messageId, Long userId, String reason);

    /**
     * 语音转文字
     *
     * @param voiceUrl 语音文件URL
     * @return 转换后的文字
     */
    String convertVoiceToText(String voiceUrl);
}
