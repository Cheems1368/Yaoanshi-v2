package com.medicine.service.ai;

import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.AiProperties;
import com.medicine.domain.ai.AiRequestAuditLog;
import com.medicine.domain.identity.AppAccount;
import com.medicine.repository.ai.AiRequestAuditLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AiAuditService {

    private static final Logger log = LoggerFactory.getLogger(AiAuditService.class);
    private static final long DEFAULT_TENANT_ID = 1L;
    private static final String REQUEST_CLASS = "MEDICATION_EDUCATION_CHAT";

    private final AiRequestAuditLogMapper auditLogMapper;
    private final AiProperties properties;

    public AiAuditService(AiRequestAuditLogMapper auditLogMapper, AiProperties properties) {
        this.auditLogMapper = auditLogMapper;
        this.properties = properties;
    }

    public AiRequestAuditLog start(AppAccount account,
                                   String requestId,
                                   String providerHost,
                                   AiRequestMetadata metadata) {
        LocalDateTime now = LocalDateTime.now();
        AiRequestAuditLog audit = new AiRequestAuditLog();
        audit.setAccountId(account.getId());
        audit.setRequestId(requestId);
        audit.setProviderCode(properties.getProviderCode());
        audit.setProviderHost(providerHost);
        audit.setModel(properties.getModel());
        audit.setRequestClass(REQUEST_CLASS);
        audit.setOriginalMessageCount(metadata.originalMessageCount());
        audit.setTransmittedMessageCount(metadata.transmittedMessageCount());
        audit.setOriginalCharCount(metadata.originalCharCount());
        audit.setTransmittedCharCount(metadata.transmittedCharCount());
        audit.setRedactionCount(metadata.redactionCount());
        audit.setStatus("PENDING");
        audit.setExpiresAt(now.plusDays(properties.getAuditRetentionDays()));
        audit.setCreator(String.valueOf(account.getId()));
        audit.setCreateTime(now);
        audit.setUpdater(String.valueOf(account.getId()));
        audit.setUpdateTime(now);
        audit.setDeleted(false);
        audit.setTenantId(account.getTenantId() == null ? DEFAULT_TENANT_ID : account.getTenantId());

        try {
            if (auditLogMapper.insert(audit) != 1 || audit.getId() == null) {
                throw new IllegalStateException("AI audit insert did not create a record");
            }
            return audit;
        } catch (RuntimeException ex) {
            throw new BusinessException(
                    ErrorCode.AI_AUDIT_UNAVAILABLE.getCode(),
                    "AI 审计暂不可用，本次问题未发送至第三方"
            );
        }
    }

    public void completeSuccess(AiRequestAuditLog audit, long latencyMs) {
        updateOutcome(audit, "SUCCESS", null, latencyMs);
    }

    public void completeFailure(AiRequestAuditLog audit, Throwable failure, long latencyMs) {
        String errorCode = failure instanceof BusinessException businessException
                ? String.valueOf(businessException.getCode())
                : String.valueOf(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode());
        updateOutcome(audit, "FAILED", errorCode, latencyMs);
    }

    private void updateOutcome(AiRequestAuditLog audit, String status, String errorCode, long latencyMs) {
        try {
            audit.setStatus(status);
            audit.setErrorCode(errorCode);
            audit.setLatencyMs(latencyMs);
            audit.setUpdateTime(LocalDateTime.now());
            if (auditLogMapper.updateOutcome(audit) != 1) {
                throw new IllegalStateException("AI audit outcome update did not match a record");
            }
        } catch (RuntimeException ex) {
            // The initial PENDING record remains as durable evidence. Never log prompt or reply content.
            log.error("Failed to finalize AI audit record requestId={} auditId={}", audit.getRequestId(), audit.getId());
        }
    }
}
