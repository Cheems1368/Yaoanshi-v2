package com.medicine.domain.identity;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class AppAccount extends BaseEntity {

    private String mobile;
    private String accountType;
    private String status;
    private String displayName;
    private String lastLoginIp;
    private LocalDateTime lastLoginTime;
    private String passwordHash;
    private LocalDateTime aiConsentGrantedAt;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getAiConsentGrantedAt() {
        return aiConsentGrantedAt;
    }

    public void setAiConsentGrantedAt(LocalDateTime aiConsentGrantedAt) {
        this.aiConsentGrantedAt = aiConsentGrantedAt;
    }
}
