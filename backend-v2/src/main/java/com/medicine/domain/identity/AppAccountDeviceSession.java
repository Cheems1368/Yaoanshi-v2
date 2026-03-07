package com.medicine.domain.identity;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class AppAccountDeviceSession extends BaseEntity {

    private Long appAccountId;
    private String deviceId;
    private String deviceName;
    private String platform;
    private String accessTokenJti;
    private String refreshTokenHash;
    private LocalDateTime refreshTokenExpiresAt;
    private LocalDateTime lastSeenAt;
    private LocalDateTime revokedAt;
    private String revokedReason;

    public Long getAppAccountId() {
        return appAccountId;
    }

    public void setAppAccountId(Long appAccountId) {
        this.appAccountId = appAccountId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getAccessTokenJti() {
        return accessTokenJti;
    }

    public void setAccessTokenJti(String accessTokenJti) {
        this.accessTokenJti = accessTokenJti;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public void setRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }

    public LocalDateTime getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }

    public void setRefreshTokenExpiresAt(LocalDateTime refreshTokenExpiresAt) {
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevokedReason() {
        return revokedReason;
    }

    public void setRevokedReason(String revokedReason) {
        this.revokedReason = revokedReason;
    }
}
