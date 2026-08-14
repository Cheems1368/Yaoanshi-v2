package com.medicine.api.dto.helporder;

public class HelpOrderAttachmentDTO {

    private Long id;
    private Long fileAssetId;
    private String attachmentType;
    private String originalFilename;
    private String mimeType;
    private Long sizeBytes;
    private String downloadUrl;
    private Integer durationSeconds;
    private HelpOrderVoiceTranscriptDTO voiceTranscript;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileAssetId() {
        return fileAssetId;
    }

    public void setFileAssetId(Long fileAssetId) {
        this.fileAssetId = fileAssetId;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public HelpOrderVoiceTranscriptDTO getVoiceTranscript() {
        return voiceTranscript;
    }

    public void setVoiceTranscript(HelpOrderVoiceTranscriptDTO voiceTranscript) {
        this.voiceTranscript = voiceTranscript;
    }
}
