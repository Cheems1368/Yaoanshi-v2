package com.medicine.api.dto.auth;

public class SessionTokenDTO {

    private String accessToken;
    private Long expiresIn;
    private Long refreshExpiresIn;
    private String deviceId;

    public static SessionTokenDTO from(TokenPair tokenPair) {
        SessionTokenDTO dto = new SessionTokenDTO();
        dto.setAccessToken(tokenPair.getAccessToken());
        dto.setExpiresIn(tokenPair.getExpiresIn());
        dto.setRefreshExpiresIn(tokenPair.getRefreshExpiresIn());
        dto.setDeviceId(tokenPair.getDeviceId());
        return dto;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    public Long getRefreshExpiresIn() { return refreshExpiresIn; }
    public void setRefreshExpiresIn(Long refreshExpiresIn) { this.refreshExpiresIn = refreshExpiresIn; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}
