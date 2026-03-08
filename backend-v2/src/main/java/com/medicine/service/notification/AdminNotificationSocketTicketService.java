package com.medicine.service.notification;

import com.medicine.config.AdminNotificationWebSocketProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AdminNotificationSocketTicketService {

    private static final String KEY_PREFIX = "admin:notification:socket-ticket:";

    private final StringRedisTemplate stringRedisTemplate;
    private final AdminNotificationWebSocketProperties websocketProperties;

    public AdminNotificationSocketTicketService(
            StringRedisTemplate stringRedisTemplate,
            AdminNotificationWebSocketProperties websocketProperties
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.websocketProperties = websocketProperties;
    }

    public Ticket issue(String token, Instant tokenExpiresAt) {
        if (!StringUtils.hasText(token)) {
            return null;
        }

        long ttlSeconds = Math.max(websocketProperties.getTicketTtlSeconds(), 1L);
        if (tokenExpiresAt != null) {
            long tokenSecondsLeft = Duration.between(Instant.now(), tokenExpiresAt).getSeconds();
            if (tokenSecondsLeft <= 0) {
                return null;
            }
            ttlSeconds = Math.min(ttlSeconds, tokenSecondsLeft);
        }

        String ticket = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(ticketKey(ticket), token, ttlSeconds, TimeUnit.SECONDS);
        return new Ticket(ticket, ttlSeconds);
    }

    public String consume(String ticket) {
        if (!StringUtils.hasText(ticket)) {
            return null;
        }
        return stringRedisTemplate.opsForValue().getAndDelete(ticketKey(ticket.trim()));
    }

    private String ticketKey(String ticket) {
        return KEY_PREFIX + ticket;
    }

    public record Ticket(String value, long expiresInSeconds) {
    }
}
