package com.medicine.domain.prescription;

import com.medicine.domain.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Prescription extends BaseEntity {

    private Long patientId;
    private String sourceType;
    private String status;
    private BigDecimal ocrConfidence;
    private Long uploadedFileAssetId;
    private String submittedByType;
    private Long submittedById;
    private Long reviewedByPharmacistId;
    private LocalDateTime reviewedAt;
    private String rejectReason;
    private Long generatedMedicationPlanId;
    private String patientName;
    private String hospitalPatientId;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getOcrConfidence() {
        return ocrConfidence;
    }

    public void setOcrConfidence(BigDecimal ocrConfidence) {
        this.ocrConfidence = ocrConfidence;
    }

    public Long getUploadedFileAssetId() {
        return uploadedFileAssetId;
    }

    public void setUploadedFileAssetId(Long uploadedFileAssetId) {
        this.uploadedFileAssetId = uploadedFileAssetId;
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

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Long getGeneratedMedicationPlanId() {
        return generatedMedicationPlanId;
    }

    public void setGeneratedMedicationPlanId(Long generatedMedicationPlanId) {
        this.generatedMedicationPlanId = generatedMedicationPlanId;
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
}
