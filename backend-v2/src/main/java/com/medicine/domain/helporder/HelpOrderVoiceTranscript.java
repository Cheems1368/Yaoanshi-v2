package com.medicine.domain.helporder;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class HelpOrderVoiceTranscript extends BaseEntity {

    private Long id;
    private Long helpOrderId;
    private Long helpOrderEventAttachmentId;
    private Long playbackFileAssetId;
    private Long transcriptSourceFileAssetId;
    private Integer durationSeconds;
    private String provider;
    private String status;
    private String transcriptText;
    private String errorCode;
    private String transcribedByType;
    private Long transcribedById;
    private LocalDateTime transcribedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHelpOrderId() {
        return helpOrderId;
    }

    public void setHelpOrderId(Long helpOrderId) {
        this.helpOrderId = helpOrderId;
    }

    public Long getHelpOrderEventAttachmentId() {
        return helpOrderEventAttachmentId;
    }

    public void setHelpOrderEventAttachmentId(Long helpOrderEventAttachmentId) {
        this.helpOrderEventAttachmentId = helpOrderEventAttachmentId;
    }

    public Long getPlaybackFileAssetId() {
        return playbackFileAssetId;
    }

    public void setPlaybackFileAssetId(Long playbackFileAssetId) {
        this.playbackFileAssetId = playbackFileAssetId;
    }

    public Long getTranscriptSourceFileAssetId() {
        return transcriptSourceFileAssetId;
    }

    public void setTranscriptSourceFileAssetId(Long transcriptSourceFileAssetId) {
        this.transcriptSourceFileAssetId = transcriptSourceFileAssetId;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
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

    public String getTranscriptText() {
        return transcriptText;
    }

    public void setTranscriptText(String transcriptText) {
        this.transcriptText = transcriptText;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getTranscribedByType() {
        return transcribedByType;
    }

    public void setTranscribedByType(String transcribedByType) {
        this.transcribedByType = transcribedByType;
    }

    public Long getTranscribedById() {
        return transcribedById;
    }

    public void setTranscribedById(Long transcribedById) {
        this.transcribedById = transcribedById;
    }

    public LocalDateTime getTranscribedAt() {
        return transcribedAt;
    }

    public void setTranscribedAt(LocalDateTime transcribedAt) {
        this.transcribedAt = transcribedAt;
    }
}
