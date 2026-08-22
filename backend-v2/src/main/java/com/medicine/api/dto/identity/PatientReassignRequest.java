package com.medicine.api.dto.identity;

public class PatientReassignRequest {
    private Long pharmacistId;
    private String reason;
    public Long getPharmacistId() { return pharmacistId; }
    public void setPharmacistId(Long pharmacistId) { this.pharmacistId = pharmacistId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
