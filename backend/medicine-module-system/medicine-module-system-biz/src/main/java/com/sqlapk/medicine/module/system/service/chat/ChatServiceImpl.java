package com.sqlapk.medicine.module.system.service.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil;
import com.sqlapk.medicine.module.system.api.ChatServiceApi;
import com.sqlapk.medicine.module.system.api.dto.chat.ChatMessageReqDTO;
import com.sqlapk.medicine.module.system.api.dto.chat.ChatMessageRespDTO;
import com.sqlapk.medicine.module.system.convert.chat.ChatMessageConvert;
import com.sqlapk.medicine.module.system.dal.dataobject.chat.ChatMessageDO;
import com.sqlapk.medicine.module.system.dal.mysql.chat.ChatMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天服务实现
 *
 * @author sqlapk
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatServiceApi {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Long sendMessage(ChatMessageReqDTO reqDTO, Long senderId, String senderType) {
        log.info("发送聊天消息，工单ID: {}, 发送者: {}, 接收者: {}", 
                reqDTO.getOrderId(), senderId, reqDTO.getReceiverId());

        // 1. 数据验证
        validateMessageData(reqDTO);

        // 2. 转换为DO对象
        ChatMessageDO messageDO = ChatMessageConvert.INSTANCE.convert(reqDTO);
        messageDO.setSenderId(senderId);
        messageDO.setSenderType(senderType);
        messageDO.setStatus("sent");
        messageDO.setSendTime(LocalDateTime.now());
        messageDO.setIsRecalled(0);

        // 3. 处理附件
        if (reqDTO.getAttachments() != null && !reqDTO.getAttachments().isEmpty()) {
            try {
                String attachmentsJson = objectMapper.writeValueAsString(reqDTO.getAttachments());
                messageDO.setAttachments(attachmentsJson);
            } catch (Exception e) {
                log.error("处理附件JSON失败", e);
                throw ServiceExceptionUtil.exception(500, "附件处理失败");
            }
        }

        // 4. 语音转文字处理
        if ("voice".equals(reqDTO.getMessageType()) && reqDTO.getVoiceUrl() != null) {
            try {
                String voiceToText = convertVoiceToText(reqDTO.getVoiceUrl());
                messageDO.setVoiceToText(voiceToText);
                // 如果语音转文字成功，同时更新消息内容
                if (voiceToText != null && !voiceToText.isEmpty()) {
                    messageDO.setContent(voiceToText);
                }
            } catch (Exception e) {
                log.warn("语音转文字失败，继续发送消息", e);
            }
        }

        // 5. 保存消息
        chatMessageMapper.insert(messageDO);

        log.info("聊天消息发送成功，消息ID: {}", messageDO.getId());
        return messageDO.getId();
    }

    @Override
    public List<ChatMessageRespDTO> getChatHistory(Long orderId) {
        log.info("获取工单聊天历史，工单ID: {}", orderId);

        List<ChatMessageDO> messages = chatMessageMapper.selectByOrderId(orderId);
        return messages.stream()
                .map(ChatMessageConvert.INSTANCE::convert)
                .collect(Collectors.toList());
    }

    @Override
    public Long getUnreadMessageCount(Long userId) {
        log.info("获取用户未读消息数量，用户ID: {}", userId);

        return chatMessageMapper.countUnreadMessages(userId);
    }

    @Override
    @Transactional
    public Integer markMessagesAsRead(Long userId, Long orderId) {
        log.info("标记消息为已读，用户ID: {}, 工单ID: {}", userId, orderId);

        return chatMessageMapper.markMessagesAsRead(userId, orderId);
    }

    @Override
    @Transactional
    public Boolean recallMessage(Long messageId, Long userId, String reason) {
        log.info("撤回消息，消息ID: {}, 用户ID: {}, 原因: {}", messageId, userId, reason);

        ChatMessageDO message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw ServiceExceptionUtil.exception(404, "消息不存在");
        }

        // 验证权限：只有发送者可以撤回
        if (!message.getSenderId().equals(userId)) {
            throw ServiceExceptionUtil.exception(403, "无权限撤回此消息");
        }

        // 检查撤回时间限制（5分钟内）
        LocalDateTime now = LocalDateTime.now();
        if (message.getSendTime().isBefore(now.minusMinutes(5))) {
            throw ServiceExceptionUtil.exception(400, "消息发送超过5分钟，无法撤回");
        }

        // 更新消息状态
        message.setIsRecalled(1);
        message.setRecallTime(now);
        message.setRecallReason(reason);
        chatMessageMapper.updateById(message);

        log.info("消息撤回成功，消息ID: {}", messageId);
        return true;
    }

    @Override
    public String convertVoiceToText(String voiceUrl) {
        log.info("语音转文字，语音URL: {}", voiceUrl);

        // TODO: 集成语音识别服务（如百度语音、阿里云语音等）
        // 这里先返回模拟结果
        try {
            // 模拟语音转文字处理时间
            Thread.sleep(1000);
            
            // 模拟转换结果
            String[] mockResults = {
                "我高血压药快吃完了，想续开",
                "这两天没什么不舒服，偶尔有点头晕，血压基本稳定",
                "最近血压控制得还可以",
                "需要调整用药剂量吗"
            };
            
            int index = Math.abs(voiceUrl.hashCode()) % mockResults.length;
            String result = mockResults[index];
            
            log.info("语音转文字完成，结果: {}", result);
            return result;
            
        } catch (Exception e) {
            log.error("语音转文字失败", e);
            return null;
        }
    }

    /**
     * 验证消息数据
     */
    private void validateMessageData(ChatMessageReqDTO reqDTO) {
        if (reqDTO.getOrderId() == null) {
            throw ServiceExceptionUtil.exception(400, "工单ID不能为空");
        }
        
        if (reqDTO.getReceiverId() == null) {
            throw ServiceExceptionUtil.exception(400, "接收者ID不能为空");
        }
        
        if (reqDTO.getMessageType() == null || reqDTO.getMessageType().isEmpty()) {
            throw ServiceExceptionUtil.exception(400, "消息类型不能为空");
        }
        
        // 根据消息类型验证内容
        if ("text".equals(reqDTO.getMessageType())) {
            if (reqDTO.getContent() == null || reqDTO.getContent().trim().isEmpty()) {
                throw ServiceExceptionUtil.exception(400, "文本消息内容不能为空");
            }
        } else if ("voice".equals(reqDTO.getMessageType())) {
            if (reqDTO.getVoiceUrl() == null || reqDTO.getVoiceUrl().trim().isEmpty()) {
                throw ServiceExceptionUtil.exception(400, "语音消息URL不能为空");
            }
        }
    }
}
