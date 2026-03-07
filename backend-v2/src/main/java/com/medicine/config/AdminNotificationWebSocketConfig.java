package com.medicine.config;

import com.medicine.service.notification.AdminNotificationWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class AdminNotificationWebSocketConfig implements WebSocketConfigurer {

    private final AdminNotificationWebSocketHandler adminNotificationWebSocketHandler;

    public AdminNotificationWebSocketConfig(AdminNotificationWebSocketHandler adminNotificationWebSocketHandler) {
        this.adminNotificationWebSocketHandler = adminNotificationWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(adminNotificationWebSocketHandler, "/ws/admin-notifications")
                .setAllowedOriginPatterns("*");
    }
}
