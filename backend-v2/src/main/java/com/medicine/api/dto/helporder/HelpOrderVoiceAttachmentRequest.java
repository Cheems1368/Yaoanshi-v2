package com.medicine.api.dto.helporder;

public class HelpOrderVoiceAttachmentRequest {

    private Long playbackFileAssetId;
    private Long transcriptSourceFileAssetId;
    private Integer durationSeconds;

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
}
