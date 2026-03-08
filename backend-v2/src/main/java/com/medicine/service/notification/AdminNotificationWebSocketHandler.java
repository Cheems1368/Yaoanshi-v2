package com.medicine.service.notification;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

@Component
public class AdminNotificationWebSocketHandler extends TextWebSocketHandler {

    private static final CloseStatus UNAUTHORIZED_STATUS = new CloseStatus(4401, "UNAUTHORIZED");

    private final AdminNotificationWebSocketAuthenticator authenticator;
    private final AdminNotificationSocketTicketService socketTicketService;
    private final AdminNotificationRealtimeService realtimeService;

    public AdminNotificationWebSocketHandler(
            AdminNotificationWebSocketAuthenticator authenticator,
            AdminNotificationSocketTicketService socketTicketService,
            AdminNotificationRealtimeService realtimeService
    ) {
        this.authenticator = authenticator;
        this.socketTicketService = socketTicketService;
        this.realtimeService = realtimeService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = null;
        if (session.getUri() != null) {
            String ticket = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams().getFirst("ticket");
            token = socketTicketService.consume(ticket);
        }
        if (token == null) {
            token = session.getHandshakeHeaders().getFirst("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }
        AdminNotificationWebSocketAuthenticator.SessionTarget target = authenticator.authenticate(token);
        if (target == null) {
            session.close(UNAUTHORIZED_STATUS);
            return;
        }
        session.getAttributes().put("targetType", target.targetType());
        session.getAttributes().put("targetId", target.targetId());
        realtimeService.register(session, target.targetType(), target.targetId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Server push only; client heartbeats can be ignored safely.
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        realtimeService.unregister(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        realtimeService.unregister(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }
}
