package com.medicine.api.dto.helporder;

public class HelpOrderVoiceTranscriptDTO {

    private Long id;
    private String provider;
    private String status;
    private String text;
    private String errorCode;
    private String transcribedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getTranscribedAt() {
        return transcribedAt;
    }

    public void setTranscribedAt(String transcribedAt) {
        this.transcribedAt = transcribedAt;
    }
}
