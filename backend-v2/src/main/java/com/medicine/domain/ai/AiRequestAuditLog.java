package com.medicine.domain.ai;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class AiRequestAuditLog extends BaseEntity {

    private Long accountId;
    private String requestId;
    private String providerCode;
    private String providerHost;
    private String model;
    private String requestClass;
    private Integer originalMessageCount;
    private Integer transmittedMessageCount;
    private Integer originalCharCount;
    private Integer transmittedCharCount;
    private Integer redactionCount;
    private String status;
    private String errorCode;
    private Long latencyMs;
    private LocalDateTime expiresAt;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderHost() { return providerHost; }
    public void setProviderHost(String providerHost) { this.providerHost = providerHost; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getRequestClass() { return requestClass; }
    public void setRequestClass(String requestClass) { this.requestClass = requestClass; }
    public Integer getOriginalMessageCount() { return originalMessageCount; }
    public void setOriginalMessageCount(Integer originalMessageCount) { this.originalMessageCount = originalMessageCount; }
    public Integer getTransmittedMessageCount() { return transmittedMessageCount; }
    public void setTransmittedMessageCount(Integer transmittedMessageCount) { this.transmittedMessageCount = transmittedMessageCount; }
    public Integer getOriginalCharCount() { return originalCharCount; }
    public void setOriginalCharCount(Integer originalCharCount) { this.originalCharCount = originalCharCount; }
    public Integer getTransmittedCharCount() { return transmittedCharCount; }
    public void setTransmittedCharCount(Integer transmittedCharCount) { this.transmittedCharCount = transmittedCharCount; }
    public Integer getRedactionCount() { return redactionCount; }
    public void setRedactionCount(Integer redactionCount) { this.redactionCount = redactionCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
