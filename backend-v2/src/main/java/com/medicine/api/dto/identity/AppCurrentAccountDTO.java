package com.medicine.api.dto.identity;

public class AppCurrentAccountDTO {

    private String mobile;
    private String displayName;
    private String accountType;
    private boolean bound;
    private String deviceId;
    private AppPatientSummaryDTO patient;
    private String lastLoginAt;

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public boolean isBound() { return bound; }
    public void setBound(boolean bound) { this.bound = bound; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public AppPatientSummaryDTO getPatient() { return patient; }
    public void setPatient(AppPatientSummaryDTO patient) { this.patient = patient; }

    public String getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(String lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
