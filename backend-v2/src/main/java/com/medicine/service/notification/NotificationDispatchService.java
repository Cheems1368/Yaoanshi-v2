package com.medicine.service.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.domain.notification.NotificationLog;
import com.medicine.repository.notification.NotificationLogMapper;
import com.medicine.repository.notification.NotificationCommitGateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    private final NotificationCommitGateMapper notificationCommitGateMapper;
    private final PlatformTransactionManager transactionManager;
    private final AdminNotificationRealtimeService adminRealtimeService;
    private final AppNotificationRealtimeService appRealtimeService;
    private final ObjectMapper objectMapper;

    public NotificationDispatchService(
            NotificationLogMapper notificationLogMapper,
            NotificationCommitGateMapper notificationCommitGateMapper,
            PlatformTransactionManager transactionManager,
            AdminNotificationRealtimeService adminRealtimeService,
            AppNotificationRealtimeService appRealtimeService,
            ObjectMapper objectMapper
    ) {
        this.notificationLogMapper = notificationLogMapper;
        this.notificationCommitGateMapper = notificationCommitGateMapper;
        this.transactionManager = transactionManager;
        this.adminRealtimeService = adminRealtimeService;
        this.appRealtimeService = appRealtimeService;
        this.objectMapper = objectMapper;
    }

    @Transactional
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
        log.setAttemptCount(0);
        log.setNextRetryAt(null);
        log.setLastAttemptAt(null);
        log.setClaimToken(null);
        log.setClaimedAt(null);
        log.setFailureCode(null);
        log.setFailureReason(null);
        log.setPayloadJson(writeJson(payload));
        applyAudit(log, tenantId, operator, now);
        notificationCommitGateMapper.lock();
        notificationLogMapper.insert(log);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        TransactionTemplate delivery = new TransactionTemplate(transactionManager);
                        delivery.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                        delivery.executeWithoutResult(status ->
                                updateDispatchResult(log, LocalDateTime.now(java.time.ZoneOffset.UTC), null));
                    } catch (RuntimeException ignored) {
                        // A stale PENDING log is claimed by the retry job.
                    }
                }
            });
        } else {
            updateDispatchResult(log, now, null);
        }
        return log;
    }

    private void updateDispatchResult(NotificationLog log, LocalDateTime now, String expectedClaimToken) {
        DispatchResult result = "WEBSOCKET".equals(log.getChannel())
                ? dispatchRealtime(log)
                : DispatchResult.sent();
        if (!result.success() && "NO_ACTIVE_SESSION".equals(result.failureCode())) {
            result = DispatchResult.sent();
            log.setChannel("IN_APP");
        }
        int attemptCount = (log.getAttemptCount() == null ? 0 : log.getAttemptCount()) + 1;
        log.setAttemptCount(attemptCount);
        log.setLastAttemptAt(now);
        log.setDeliveryStatus(result.success() ? "SENT" : attemptCount >= 4 ? "PERMANENT_FAILED" : "FAILED");
        log.setSentAt(result.success() ? now : null);
        log.setFailureCode(result.success() ? null : result.failureCode());
        log.setFailureReason(result.success() ? null : result.failureReason());
        log.setNextRetryAt(result.success() || attemptCount >= 4 ? null : nextRetryAt(now, attemptCount));
        log.setClaimToken(null);
        log.setClaimedAt(null);
        log.setUpdater("notification-dispatch");
        log.setUpdateTime(now);
        if (expectedClaimToken == null) {
            notificationLogMapper.updateDelivery(log);
        } else {
            notificationLogMapper.updateClaimedDelivery(log, expectedClaimToken);
        }
    }

    public void retryClaimed(NotificationLog log, LocalDateTime now) {
        if (log == null || !("FAILED".equals(log.getDeliveryStatus()) || "PENDING".equals(log.getDeliveryStatus()))
                || log.getClaimToken() == null) {
            return;
        }
        updateDispatchResult(log, now, log.getClaimToken());
    }

    private LocalDateTime nextRetryAt(LocalDateTime now, int attemptCount) {
        return now.plusMinutes(switch (attemptCount) {
            case 1 -> 1;
            case 2 -> 5;
            default -> 15;
        });
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
