package com.medicine.api.dto.helporder;

import java.util.List;

public class HelpOrderReplyRequest {

    private String content;
    private List<Long> attachmentFileAssetIds;
    private HelpOrderVoiceAttachmentRequest voiceAttachment;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Long> getAttachmentFileAssetIds() {
        return attachmentFileAssetIds;
    }

    public void setAttachmentFileAssetIds(List<Long> attachmentFileAssetIds) {
        this.attachmentFileAssetIds = attachmentFileAssetIds;
    }

    public HelpOrderVoiceAttachmentRequest getVoiceAttachment() {
        return voiceAttachment;
    }

    public void setVoiceAttachment(HelpOrderVoiceAttachmentRequest voiceAttachment) {
        this.voiceAttachment = voiceAttachment;
    }
}
