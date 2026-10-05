package com.medicine.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminNotificationWebSocketHandlerTest {

    @Mock
    private AdminNotificationWebSocketAuthenticator authenticator;

    @Mock
    private AdminNotificationSocketTicketService socketTicketService;

    @Mock
    private AdminNotificationRealtimeService realtimeService;

    @Mock
    private WebSocketSession session;

    @Test
    void shouldAuthenticateWithSocketTicketFirst() throws Exception {
        AdminNotificationWebSocketHandler handler = new AdminNotificationWebSocketHandler(
                authenticator,
                socketTicketService,
                realtimeService
        );
        Map<String, Object> attributes = new HashMap<>();

        when(session.getUri()).thenReturn(URI.create("ws://localhost/ws/admin-notifications?ticket=ticket-1"));
        when(session.getAttributes()).thenReturn(attributes);
        when(socketTicketService.consume("ticket-1")).thenReturn("ticket-token");
        when(authenticator.authenticate("ticket-token"))
                .thenReturn(new AdminNotificationWebSocketAuthenticator.SessionTarget(1L, "PHARMACIST", 11L));

        handler.afterConnectionEstablished(session);

        assertThat(attributes).containsEntry("targetType", "PHARMACIST");
        assertThat(attributes).containsEntry("targetId", 11L);
        verify(realtimeService).register(session, 1L, "PHARMACIST", 11L);
    }

    @Test
    void shouldFallBackToAuthorizationHeaderWhenTicketMissing() throws Exception {
        AdminNotificationWebSocketHandler handler = new AdminNotificationWebSocketHandler(
                authenticator,
                socketTicketService,
                realtimeService
        );
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer header-token");
        Map<String, Object> attributes = new HashMap<>();

        when(session.getUri()).thenReturn(URI.create("ws://localhost/ws/admin-notifications"));
        when(session.getHandshakeHeaders()).thenReturn(headers);
        when(session.getAttributes()).thenReturn(attributes);
        when(socketTicketService.consume(null)).thenReturn(null);
        when(authenticator.authenticate("header-token"))
                .thenReturn(new AdminNotificationWebSocketAuthenticator.SessionTarget(1L, "DOCTOR", 22L));

        handler.afterConnectionEstablished(session);

        assertThat(attributes).containsEntry("targetType", "DOCTOR");
        assertThat(attributes).containsEntry("targetId", 22L);
        verify(realtimeService).register(session, 1L, "DOCTOR", 22L);
    }

    @Test
    void shouldCloseSessionWhenAuthenticationFails() throws Exception {
        AdminNotificationWebSocketHandler handler = new AdminNotificationWebSocketHandler(
                authenticator,
                socketTicketService,
                realtimeService
        );

        when(session.getUri()).thenReturn(URI.create("ws://localhost/ws/admin-notifications"));
        when(session.getHandshakeHeaders()).thenReturn(new HttpHeaders());
        when(authenticator.authenticate(null)).thenReturn(null);

        handler.afterConnectionEstablished(session);

        verify(session).close(argThat(status -> status.getCode() == 4401 && "UNAUTHORIZED".equals(status.getReason())));
    }

    @Test
    void shouldUnregisterWhenConnectionClosedOrTransportFails() throws Exception {
        AdminNotificationWebSocketHandler handler = new AdminNotificationWebSocketHandler(
                authenticator,
                socketTicketService,
                realtimeService
        );

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        when(session.isOpen()).thenReturn(true);
        handler.handleTransportError(session, new IllegalStateException("boom"));

        verify(realtimeService, times(2)).unregister(session);
        verify(session).close(CloseStatus.SERVER_ERROR);
    }
}
