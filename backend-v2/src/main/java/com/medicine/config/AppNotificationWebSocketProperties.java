package com.medicine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "medicine.app-notifications.websocket")
public class AppNotificationWebSocketProperties {

    private List<String> allowedOrigins = new ArrayList<>(List.of(
            "http://localhost:3001",
            "http://127.0.0.1:3001",
            "http://localhost:5173",
            "http://127.0.0.1:5173"
    ));

    private long ticketTtlSeconds = 30;

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins == null ? new ArrayList<>() : new ArrayList<>(allowedOrigins);
    }

    public long getTicketTtlSeconds() {
        return ticketTtlSeconds;
    }

    public void setTicketTtlSeconds(long ticketTtlSeconds) {
        this.ticketTtlSeconds = ticketTtlSeconds;
    }
}
