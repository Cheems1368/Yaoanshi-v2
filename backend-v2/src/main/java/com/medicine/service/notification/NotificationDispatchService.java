package com.medicine.service.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.domain.notification.NotificationLog;
import com.medicine.repository.notification.NotificationLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
public class NotificationDispatchService {

    private static final Set<String> ADMIN_REALTIME_BUSINESS_TYPES = Set.of(
            "HELP_ORDER_SUBMITTED",
            "HELP_ORDER_PATIENT_REPLY",
            "HELP_ORDER_ESCALATED",
            "HELP_ORDER_REPLY",
            "HELP_ORDER_CLOSED"
    );

    private static final Set<String> APP_REALTIME_BUSINESS_TYPES = Set.of(
            "HELP_ORDER_CLAIMED",
            "HELP_ORDER_REPLY",
            "HELP_ORDER_ESCALATED",
            "HELP_ORDER_REJECTED",
            "HELP_ORDER_CLOSED",
            "SELF_MEDICATION_ACKNOWLEDGED",
            "SELF_MEDICATION_REJECTED"
    );

    private final NotificationLogMapper notificationLogMapper;
    private final AdminNotificationRealtimeService adminRealtimeService;
    private final AppNotificationRealtimeService appRealtimeService;
    private final ObjectMapper objectMapper;

    public NotificationDispatchService(
            NotificationLogMapper notificationLogMapper,
            AdminNotificationRealtimeService adminRealtimeService,
            AppNotificationRealtimeService appRealtimeService,
            ObjectMapper objectMapper
    ) {
        this.notificationLogMapper = notificationLogMapper;
        this.adminRealtimeService = adminRealtimeService;
        this.appRealtimeService = appRealtimeService;
        this.objectMapper = objectMapper;
    }

    public NotificationLog createAndDispatch(
            Long tenantId,
            String operator,
            String targetType,
            Long targetId,
            String businessType,
            Long businessId,
            Map<String, Object> payload,
            LocalDateTime now
    ) {
        NotificationLog log = new NotificationLog();
        log.setTargetType(targetType);
        log.setTargetId(targetId == null ? 0L : targetId);
        log.setChannel(resolveChannel(targetType, businessType));
        log.setBusinessType(businessType);
        log.setBusinessId(businessId);
        log.setScheduledAt(now);
        log.setSentAt(null);
        log.setDeliveryStatus("PENDING");
        log.setFailureCode(null);
        log.setFailureReason(null);
        log.setPayloadJson(writeJson(payload));
        applyAudit(log, tenantId, operator, now);
        notificationLogMapper.insert(log);
        updateDispatchResult(log, now);
        return log;
    }

    private void updateDispatchResult(NotificationLog log, LocalDateTime now) {
        DispatchResult result = "WEBSOCKET".equals(log.getChannel())
                ? dispatchRealtime(log)
                : DispatchResult.sent();
        // Patient WS dispatch failure degrades to SENT: the notification is persisted
        // and the patient client will pick it up via polling fallback.
        if (!result.success() && "PATIENT".equals(log.getTargetType())) {
            result = DispatchResult.sent();
            log.setChannel("IN_APP");
        }
        log.setDeliveryStatus(result.success() ? "SENT" : "FAILED");
        log.setSentAt(result.success() ? now : null);
        log.setFailureCode(result.success() ? null : result.failureCode());
        log.setFailureReason(result.success() ? null : result.failureReason());
        log.setUpdater("notification-dispatch");
        log.setUpdateTime(now);
        notificationLogMapper.updateDelivery(log);
    }

    private String resolveChannel(String targetType, String businessType) {
        if (("PHARMACIST".equals(targetType) || "DOCTOR".equals(targetType))
                && ADMIN_REALTIME_BUSINESS_TYPES.contains(businessType)) {
            return "WEBSOCKET";
        }
        if ("PATIENT".equals(targetType) && APP_REALTIME_BUSINESS_TYPES.contains(businessType)) {
            return "WEBSOCKET";
        }
        return "IN_APP";
    }

    private DispatchResult dispatchRealtime(NotificationLog log) {
        if ("PATIENT".equals(log.getTargetType())) {
            return appRealtimeService.dispatch(log);
        }
        return adminRealtimeService.dispatch(log);
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private void applyAudit(NotificationLog entity, Long tenantId, String operator, LocalDateTime now) {
        entity.setCreator(operator);
        entity.setCreateTime(now);
        entity.setUpdater(operator);
        entity.setUpdateTime(now);
        entity.setDeleted(false);
        entity.setTenantId(tenantId);
    }

    public record DispatchResult(boolean success, String failureCode, String failureReason) {
        public static DispatchResult sent() {
            return new DispatchResult(true, null, null);
        }

        public static DispatchResult failed(String failureCode, String failureReason) {
            return new DispatchResult(false, failureCode, failureReason);
        }
    }
}
