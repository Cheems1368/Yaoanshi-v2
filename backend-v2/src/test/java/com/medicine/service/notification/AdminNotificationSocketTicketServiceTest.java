package com.medicine.service.notification;

import com.medicine.config.AdminNotificationWebSocketProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminNotificationSocketTicketServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private AdminNotificationSocketTicketService ticketService;

    @BeforeEach
    void setUp() {
        AdminNotificationWebSocketProperties properties = new AdminNotificationWebSocketProperties();
        properties.setTicketTtlSeconds(30);
        ticketService = new AdminNotificationSocketTicketService(stringRedisTemplate, properties);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldIssueTicketUsingConfiguredTtl() {
        AdminNotificationSocketTicketService.Ticket ticket = ticketService.issue("legacy-access-token", null);

        assertThat(ticket).isNotNull();
        assertThat(ticket.expiresInSeconds()).isEqualTo(30L);
        verify(valueOperations).set(anyString(), eq("legacy-access-token"), eq(30L), eq(TimeUnit.SECONDS));
    }

    @Test
    void shouldCapTicketTtlByTokenExpiry() {
        AdminNotificationSocketTicketService.Ticket ticket = ticketService.issue(
                "legacy-access-token",
                Instant.now().plusSeconds(5)
        );

        assertThat(ticket).isNotNull();
        assertThat(ticket.expiresInSeconds()).isBetween(1L, 5L);
        verify(valueOperations).set(anyString(), eq("legacy-access-token"), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void shouldConsumeTicketOnce() {
        when(valueOperations.getAndDelete("admin:notification:socket-ticket:ticket-1")).thenReturn("legacy-access-token");

        String token = ticketService.consume("ticket-1");

        assertThat(token).isEqualTo("legacy-access-token");
        verify(valueOperations).getAndDelete("admin:notification:socket-ticket:ticket-1");
    }
}
