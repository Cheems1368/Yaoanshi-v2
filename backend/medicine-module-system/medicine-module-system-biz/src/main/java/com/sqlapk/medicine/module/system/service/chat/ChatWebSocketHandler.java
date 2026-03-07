package com.sqlapk.medicine.module.system.service.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天WebSocket处理器
 *
 * @author sqlapk
 */
@Component
@Slf4j
public class ChatWebSocketHandler implements WebSocketHandler {

    @Autowired
    private ChatServiceImpl chatService;

    @Autowired
    private ObjectMapper objectMapper;

    // 存储WebSocket连接，key为工单ID，value为连接集合
    private final Map<Long, Map<String, WebSocketSession>> orderConnections = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket连接建立，会话ID: {}", session.getId());
        
        // 从URI中提取工单ID
        String path = session.getUri().getPath();
        String orderIdStr = path.substring(path.lastIndexOf('/') + 1);
        Long orderId = Long.parseLong(orderIdStr);
        
        // 获取用户ID（从session属性中获取）
        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) {
            log.warn("WebSocket连接缺少用户ID，关闭连接");
            session.close();
            return;
        }
        
        // 存储连接
        orderConnections.computeIfAbsent(orderId, k -> new ConcurrentHashMap<>())
                .put(userId, session);
        
        log.info("用户 {} 加入工单 {} 的聊天室", userId, orderId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.info("收到WebSocket消息，会话ID: {}, 消息: {}", session.getId(), message.getPayload());
        
        try {
            // 解析消息
            String payload = (String) message.getPayload();
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            
            String messageType = (String) messageData.get("type");
            String content = (String) messageData.get("content");
            String voiceUrl = (String) messageData.get("voiceUrl");
            
            // 获取工单ID
            String path = session.getUri().getPath();
            String orderIdStr = path.substring(path.lastIndexOf('/') + 1);
            Long orderId = Long.parseLong(orderIdStr);
            
            // 获取用户信息
            String userId = (String) session.getAttributes().get("userId");
            String userType = (String) session.getAttributes().get("userType");
            
            // 构建消息请求
            com.sqlapk.medicine.module.system.api.dto.chat.ChatMessageReqDTO reqDTO = 
                    new com.sqlapk.medicine.module.system.api.dto.chat.ChatMessageReqDTO();
            reqDTO.setOrderId(orderId);
            reqDTO.setMessageType(messageType);
            reqDTO.setContent(content);
            reqDTO.setVoiceUrl(voiceUrl);
            
            // 这里需要根据业务逻辑确定接收者
            // 简化处理：假设工单中只有患者和药师
            Long receiverId = determineReceiver(orderId, userId);
            String receiverType = determineReceiverType(userType);
            
            reqDTO.setReceiverId(receiverId);
            reqDTO.setReceiverType(receiverType);
            
            // 发送消息
            Long messageId = chatService.sendMessage(reqDTO, Long.parseLong(userId), userType);
            
            // 广播消息给工单相关的所有用户
            broadcastMessage(orderId, messageData, userId);
            
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            // 发送错误消息给发送者
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"消息发送失败\"}"));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误，会话ID: {}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket连接关闭，会话ID: {}, 状态: {}", session.getId(), closeStatus);
        
        // 从连接映射中移除
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            orderConnections.values().forEach(connections -> connections.remove(userId));
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 广播消息给工单相关的所有用户
     */
    private void broadcastMessage(Long orderId, Map<String, Object> messageData, String senderUserId) {
        Map<String, WebSocketSession> connections = orderConnections.get(orderId);
        if (connections == null) {
            return;
        }
        
        try {
            String messageJson = objectMapper.writeValueAsString(messageData);
            TextMessage message = new TextMessage(messageJson);
            
            connections.forEach((userId, session) -> {
                try {
                    if (session.isOpen() && !userId.equals(senderUserId)) {
                        session.sendMessage(message);
                    }
                } catch (Exception e) {
                    log.error("发送消息给用户 {} 失败", userId, e);
                }
            });
        } catch (Exception e) {
            log.error("广播消息失败", e);
        }
    }

    /**
     * 确定接收者ID（简化实现）
     */
    private Long determineReceiver(Long orderId, String senderUserId) {
        // TODO: 根据工单信息确定接收者
        // 这里简化处理，返回一个默认值
        return 1L;
    }

    /**
     * 确定接收者类型（简化实现）
     */
    private String determineReceiverType(String senderType) {
        if ("patient".equals(senderType)) {
            return "pharmacist";
        } else if ("pharmacist".equals(senderType)) {
            return "patient";
        }
        return "patient";
    }
}
