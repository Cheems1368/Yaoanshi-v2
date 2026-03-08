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
    private final AdminNotificationWebSocketProperties websocketProperties;

    public AdminNotificationWebSocketConfig(
            AdminNotificationWebSocketHandler adminNotificationWebSocketHandler,
            AdminNotificationWebSocketProperties websocketProperties
    ) {
        this.adminNotificationWebSocketHandler = adminNotificationWebSocketHandler;
        this.websocketProperties = websocketProperties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] allowedOrigins = websocketProperties.getAllowedOrigins().toArray(String[]::new);
        registry.addHandler(adminNotificationWebSocketHandler, "/ws/admin-notifications")
                .setAllowedOrigins(allowedOrigins);
    }
}
