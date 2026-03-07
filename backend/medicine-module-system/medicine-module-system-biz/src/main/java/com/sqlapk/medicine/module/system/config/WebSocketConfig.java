package com.sqlapk.medicine.module.system.config;

import com.sqlapk.medicine.module.system.service.chat.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置
 *
 * @author sqlapk
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册聊天WebSocket处理器
        registry.addHandler(chatWebSocketHandler, "/ws/chat/{orderId}")
                .setAllowedOrigins("*"); // 生产环境需要限制域名
    }
}
