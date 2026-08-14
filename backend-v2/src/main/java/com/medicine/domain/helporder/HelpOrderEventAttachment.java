package com.medicine.domain.helporder;

import com.medicine.domain.common.BaseEntity;

public class HelpOrderEventAttachment extends BaseEntity {

    private Long helpOrderEventId;
    private Long fileAssetId;
    private String attachmentType;
    private Integer sortOrder;

    public Long getHelpOrderEventId() {
        return helpOrderEventId;
    }

    public void setHelpOrderEventId(Long helpOrderEventId) {
        this.helpOrderEventId = helpOrderEventId;
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

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
