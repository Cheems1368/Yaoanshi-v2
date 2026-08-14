package com.medicine.domain.prescription;

import com.medicine.domain.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PrescriptionOcrRecord extends BaseEntity {

    private Long prescriptionId;
    private String provider;
    private String requestPayloadJson;
    private String responsePayloadJson;
    private String extractedText;
    private String failureReason;
    private String structuredItemsJson;
    private BigDecimal confidence;
    private String status;
    private Boolean manualFallbackUsed;
    private LocalDateTime executedAt;

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getRequestPayloadJson() {
        return requestPayloadJson;
    }

    public void setRequestPayloadJson(String requestPayloadJson) {
        this.requestPayloadJson = requestPayloadJson;
    }

    public String getResponsePayloadJson() {
        return responsePayloadJson;
    }

    public void setResponsePayloadJson(String responsePayloadJson) {
        this.responsePayloadJson = responsePayloadJson;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getStructuredItemsJson() {
        return structuredItemsJson;
    }

    public void setStructuredItemsJson(String structuredItemsJson) {
        this.structuredItemsJson = structuredItemsJson;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getManualFallbackUsed() {
        return manualFallbackUsed;
    }

    public void setManualFallbackUsed(Boolean manualFallbackUsed) {
        this.manualFallbackUsed = manualFallbackUsed;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
}
