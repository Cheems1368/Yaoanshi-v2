package com.medicine.api.dto.prescription;

import java.util.List;

public class PrescriptionDTO {

    private Long id;
    private Long patientId;
    private String patientName;
    private String hospitalPatientId;
    private String status;
    private String sourceType;
    private String submittedByType;
    private Long submittedById;
    private Long reviewedByPharmacistId;
    private String reviewedAt;
    private String createdAt;
    private String rejectReason;
    private String ocrStatus;
    private String ocrConfidence;
    private List<AttachmentDTO> attachments;
    private List<ItemDTO> items;
    private List<DdiScreeningDTO> pendingDdiScreenings;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getHospitalPatientId() {
        return hospitalPatientId;
    }

    public void setHospitalPatientId(String hospitalPatientId) {
        this.hospitalPatientId = hospitalPatientId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSubmittedByType() {
        return submittedByType;
    }

    public void setSubmittedByType(String submittedByType) {
        this.submittedByType = submittedByType;
    }

    public Long getSubmittedById() {
        return submittedById;
    }

    public void setSubmittedById(Long submittedById) {
        this.submittedById = submittedById;
    }

    public Long getReviewedByPharmacistId() {
        return reviewedByPharmacistId;
    }

    public void setReviewedByPharmacistId(Long reviewedByPharmacistId) {
        this.reviewedByPharmacistId = reviewedByPharmacistId;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(String reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getOcrStatus() {
        return ocrStatus;
    }

    public void setOcrStatus(String ocrStatus) {
        this.ocrStatus = ocrStatus;
    }

    public String getOcrConfidence() {
        return ocrConfidence;
    }

    public void setOcrConfidence(String ocrConfidence) {
        this.ocrConfidence = ocrConfidence;
    }

    public List<AttachmentDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentDTO> attachments) {
        this.attachments = attachments;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }

    public List<DdiScreeningDTO> getPendingDdiScreenings() {
        return pendingDdiScreenings;
    }

    public void setPendingDdiScreenings(List<DdiScreeningDTO> pendingDdiScreenings) {
        this.pendingDdiScreenings = pendingDdiScreenings;
    }

    public static class AttachmentDTO {
        private Long id;
        private Long fileAssetId;
        private String attachmentType;
        private String originalFilename;
        private String mimeType;
        private Long sizeBytes;
        private String objectKey;
        private String downloadUrl;

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

        public String getObjectKey() {
            return objectKey;
        }

        public void setObjectKey(String objectKey) {
            this.objectKey = objectKey;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }
    }

    public static class ItemDTO {
        private Long id;
        private String drugName;
        private String drugSpec;
        private String dosage;
        private String frequency;
        private String route;
        private Integer durationDays;
        private String source;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDrugName() {
            return drugName;
        }

        public void setDrugName(String drugName) {
            this.drugName = drugName;
        }

        public String getDrugSpec() {
            return drugSpec;
        }

        public void setDrugSpec(String drugSpec) {
            this.drugSpec = drugSpec;
        }

        public String getDosage() {
            return dosage;
        }

        public void setDosage(String dosage) {
            this.dosage = dosage;
        }

        public String getFrequency() {
            return frequency;
        }

        public void setFrequency(String frequency) {
            this.frequency = frequency;
        }

        public String getRoute() {
            return route;
        }

        public void setRoute(String route) {
            this.route = route;
        }

        public Integer getDurationDays() {
            return durationDays;
        }

        public void setDurationDays(Integer durationDays) {
            this.durationDays = durationDays;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }

    public static class DdiScreeningDTO {
        private Long id;
        private Long matchedRuleId;
        private String severity;
        private String confirmStatus;
        private String message;
        private String recommendedAction;
        private String confirmationNote;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getMatchedRuleId() {
            return matchedRuleId;
        }

        public void setMatchedRuleId(Long matchedRuleId) {
            this.matchedRuleId = matchedRuleId;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getConfirmStatus() {
            return confirmStatus;
        }

        public void setConfirmStatus(String confirmStatus) {
            this.confirmStatus = confirmStatus;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getRecommendedAction() {
            return recommendedAction;
        }

        public void setRecommendedAction(String recommendedAction) {
            this.recommendedAction = recommendedAction;
        }

        public String getConfirmationNote() {
            return confirmationNote;
        }

        public void setConfirmationNote(String confirmationNote) {
            this.confirmationNote = confirmationNote;
        }
    }
}
