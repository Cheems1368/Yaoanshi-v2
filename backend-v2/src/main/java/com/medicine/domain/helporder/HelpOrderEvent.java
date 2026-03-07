package com.medicine.domain.helporder;

import com.medicine.domain.common.BaseEntity;

public class HelpOrderEvent extends BaseEntity {

    private Long helpOrderId;
    private String eventType;
    private String fromStatus;
    private String toStatus;
    private String operatorType;
    private Long operatorId;
    private String content;
    private Long attachmentFileAssetId;
    private String metadataJson;

    public Long getHelpOrderId() {
        return helpOrderId;
    }

    public void setHelpOrderId(Long helpOrderId) {
        this.helpOrderId = helpOrderId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public String getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(String operatorType) {
        this.operatorType = operatorType;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getAttachmentFileAssetId() {
        return attachmentFileAssetId;
    }

    public void setAttachmentFileAssetId(Long attachmentFileAssetId) {
        this.attachmentFileAssetId = attachmentFileAssetId;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }
}
