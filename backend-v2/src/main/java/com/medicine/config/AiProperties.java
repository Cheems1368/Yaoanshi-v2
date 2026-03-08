package com.medicine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "medicine.ai")
public class AiProperties {

    public enum Protocol {
        OPENAI_CHAT,
        ANTHROPIC_MESSAGES
    }

    private String baseUrl = "https://api.siliconflow.cn/v1/chat/completions";
    private String apiKey = "";
    private String model = "zai-org/GLM-4.5-Air";
    private long timeoutMs = 15000;
    private boolean enableThinking = false;
    private Integer maxTokens = 2048;
    private int upstreamMaxMessages = 10;
    private Protocol protocol = Protocol.OPENAI_CHAT;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean isEnableThinking() {
        return enableThinking;
    }

    public void setEnableThinking(boolean enableThinking) {
        this.enableThinking = enableThinking;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public int getUpstreamMaxMessages() {
        return upstreamMaxMessages;
    }

    public void setUpstreamMaxMessages(int upstreamMaxMessages) {
        this.upstreamMaxMessages = upstreamMaxMessages;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
}
