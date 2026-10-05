package com.medicine.service.notification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.domain.notification.NotificationLog;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminNotificationRealtimeService {

    private final ObjectMapper objectMapper;
    private final Map<String, Subscription> sessionSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, Set<WebSocketSession>> subscriptions = new ConcurrentHashMap<>();

    public AdminNotificationRealtimeService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(WebSocketSession session, Long tenantId, String targetType, Long targetId) {
        Subscription subscription = new Subscription(tenantId, targetType, targetId);
        sessionSubscriptions.put(session.getId(), subscription);
        subscriptions.computeIfAbsent(subscription.key(), ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(WebSocketSession session) {
        if (session == null) {
            return;
        }
        Subscription subscription = sessionSubscriptions.remove(session.getId());
        if (subscription == null) {
            return;
        }
        Set<WebSocketSession> sessions = subscriptions.get(subscription.key());
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            subscriptions.remove(subscription.key());
        }
    }

    public NotificationDispatchService.DispatchResult dispatch(NotificationLog log) {
        Set<WebSocketSession> targets = resolveTargets(log);
        if (targets == null || targets.isEmpty()) {
            return NotificationDispatchService.DispatchResult.failed("NO_ACTIVE_SESSION", "No active WebSocket session");
        }

        String message;
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("logId", log.getId());
            body.put("businessType", safe(log.getBusinessType()));
            body.put("businessId", log.getBusinessId());
            body.put("targetType", safe(log.getTargetType()));
            body.put("payload", parsePayload(log.getPayloadJson()));
            body.put("createdAt", log.getCreateTime() == null ? null : log.getCreateTime().toInstant(ZoneOffset.UTC).toString());
            message = objectMapper.writeValueAsString(body);
        } catch (Exception ex) {
            return NotificationDispatchService.DispatchResult.failed("PAYLOAD_SERIALIZE_FAILED", ex.getMessage());
        }

        int delivered = 0;
        for (WebSocketSession session : targets) {
            if (session == null || !session.isOpen()) {
                continue;
            }
            try {
                session.sendMessage(new TextMessage(message));
                delivered++;
            } catch (IOException ex) {
                unregister(session);
                try {
                    session.close();
                } catch (IOException ignored) {
                }
            }
        }
        if (delivered > 0) {
            return NotificationDispatchService.DispatchResult.sent();
        }
        return NotificationDispatchService.DispatchResult.failed("SOCKET_SEND_FAILED", "All WebSocket sessions failed");
    }

    private Set<WebSocketSession> resolveTargets(NotificationLog log) {
        if (log == null) {
            return Set.of();
        }
        if (log.getTargetId() != null && log.getTargetId() > 0) {
            return subscriptions.get(Subscription.key(log.getTenantId(), log.getTargetType(), log.getTargetId()));
        }
        if (log.getTargetType() == null || log.getTargetType().isBlank()) {
            return Set.of();
        }

        Set<WebSocketSession> matches = new LinkedHashSet<>();
        String prefix = log.getTenantId() + ":" + log.getTargetType() + ":";
        subscriptions.forEach((key, sessions) -> {
            if (key.startsWith(prefix) && sessions != null) {
                matches.addAll(sessions);
            }
        });
        return matches;
    }

    private Object parsePayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(payloadJson, new TypeReference<Object>() {
            });
        } catch (Exception ex) {
            return Map.of("raw", payloadJson);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record Subscription(Long tenantId, String targetType, Long targetId) {
        private String key() {
            return key(tenantId, targetType, targetId);
        }

        private static String key(Long tenantId, String targetType, Long targetId) {
            return tenantId + ":" + (targetType == null ? "UNKNOWN" : targetType) + ":" + (targetId == null ? 0L : targetId);
        }
    }
}
