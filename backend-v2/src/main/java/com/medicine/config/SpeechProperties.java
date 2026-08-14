package com.medicine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "medicine.speech.xfyun")
public class SpeechProperties {

    private String appId = "";
    private String apiKey = "";
    private String apiSecret = "";
    private String baseUrl = "wss://iat-api.xfyun.cn/v2/iat";
    private String primaryDomain = "xfime-mianqie";
    private String fallbackDomain = "iat";
    private long timeoutMs = 30000;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getPrimaryDomain() {
        return primaryDomain;
    }

    public void setPrimaryDomain(String primaryDomain) {
        this.primaryDomain = primaryDomain;
    }

    public String getFallbackDomain() {
        return fallbackDomain;
    }

    public void setFallbackDomain(String fallbackDomain) {
        this.fallbackDomain = fallbackDomain;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}
