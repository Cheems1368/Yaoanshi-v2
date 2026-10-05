package com.medicine.api.dto.medication;

import java.util.List;
import java.util.Map;

public class PlanActivationRequest {
    private List<String> acknowledgedRiskKeys;
    private Map<Long, String> firstExecutionAtByItemId;
    private String conflictReason;

    public Map<Long, String> getFirstExecutionAtByItemId() {
        return firstExecutionAtByItemId;
    }

    public void setFirstExecutionAtByItemId(Map<Long, String> firstExecutionAtByItemId) {
        this.firstExecutionAtByItemId = firstExecutionAtByItemId;
    }

    public String getConflictReason() {
        return conflictReason;
    }

    public void setConflictReason(String conflictReason) {
        this.conflictReason = conflictReason;
    }

    public List<String> getAcknowledgedRiskKeys() {
        return acknowledgedRiskKeys;
    }

    public void setAcknowledgedRiskKeys(List<String> acknowledgedRiskKeys) {
        this.acknowledgedRiskKeys = acknowledgedRiskKeys;
    }
}
