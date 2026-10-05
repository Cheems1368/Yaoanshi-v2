package com.medicine.service.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.domain.notification.NotificationLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminNotificationRealtimeServiceTest {

    @Mock
    private WebSocketSession pharmacistSession;

    @Mock
    private WebSocketSession doctorSession;

    @Test
    void shouldBroadcastProfessionalNotificationWhenTargetIdIsZero() throws IOException {
        AdminNotificationRealtimeService realtimeService = new AdminNotificationRealtimeService(new ObjectMapper());
        when(pharmacistSession.getId()).thenReturn("pharmacist-1");
        when(pharmacistSession.isOpen()).thenReturn(true);
        when(doctorSession.getId()).thenReturn("doctor-1");
        realtimeService.register(pharmacistSession, 1L, "PHARMACIST", 1L);
        realtimeService.register(doctorSession, 1L, "DOCTOR", 2L);

        NotificationLog log = new NotificationLog();
        log.setTargetType("PHARMACIST");
        log.setTenantId(1L);
        log.setId(100L);
        log.setTargetId(0L);
        log.setBusinessType("HELP_ORDER_SUBMITTED");
        log.setBusinessId(9001L);
        log.setPayloadJson("{\"orderNo\":\"HO-9001\"}");
        log.setCreateTime(LocalDateTime.now());

        NotificationDispatchService.DispatchResult result = realtimeService.dispatch(log);

        assertThat(result.success()).isTrue();
        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(pharmacistSession).sendMessage(messageCaptor.capture());
        verify(doctorSession, never()).sendMessage(any(TextMessage.class));
        assertThat(messageCaptor.getValue().getPayload()).contains("HELP_ORDER_SUBMITTED");
        assertThat(messageCaptor.getValue().getPayload()).contains("HO-9001");
    }
}
