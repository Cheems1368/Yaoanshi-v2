package com.medicine.config;

import com.medicine.service.notification.AppNotificationWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class AppNotificationWebSocketConfig implements WebSocketConfigurer {

    private final AppNotificationWebSocketHandler appNotificationWebSocketHandler;
    private final AppNotificationWebSocketProperties websocketProperties;

    public AppNotificationWebSocketConfig(
            AppNotificationWebSocketHandler appNotificationWebSocketHandler,
            AppNotificationWebSocketProperties websocketProperties
    ) {
        this.appNotificationWebSocketHandler = appNotificationWebSocketHandler;
        this.websocketProperties = websocketProperties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] allowedOrigins = websocketProperties.getAllowedOrigins().toArray(String[]::new);
        registry.addHandler(appNotificationWebSocketHandler, "/ws/app-notifications")
                .setAllowedOrigins(allowedOrigins);
    }
}
