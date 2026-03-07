package com.medicine.domain.prescription;

import com.medicine.domain.common.BaseEntity;

public class PrescriptionAttachment extends BaseEntity {

    private Long prescriptionId;
    private Long fileAssetId;
    private String attachmentType;

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
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
}
