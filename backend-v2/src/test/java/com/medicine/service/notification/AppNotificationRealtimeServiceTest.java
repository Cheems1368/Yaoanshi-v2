package com.medicine.service.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.domain.notification.NotificationLog;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppNotificationRealtimeServiceTest {

    @Test
    void shouldIncludeNotificationLogIdInRealtimePayload() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        AppNotificationRealtimeService service = new AppNotificationRealtimeService(objectMapper);
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("session-1");
        when(session.isOpen()).thenReturn(true);
        service.register(session, "PATIENT", 7L);

        NotificationLog log = new NotificationLog();
        log.setId(51L);
        log.setTargetType("PATIENT");
        log.setTargetId(7L);
        log.setBusinessType("HELP_ORDER");
        log.setBusinessId(40L);
        log.setPayloadJson("{\"status\":\"CLOSED\"}");
        log.setCreateTime(LocalDateTime.of(2026, 9, 20, 8, 0));

        assertThat(service.dispatch(log).success()).isTrue();
        var messageCaptor = org.mockito.ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(messageCaptor.capture());
        JsonNode payload = objectMapper.readTree(messageCaptor.getValue().getPayload());
        assertThat(payload.path("id").asLong()).isEqualTo(51L);
        assertThat(payload.path("payload").path("status").asText()).isEqualTo("CLOSED");
    }
}
