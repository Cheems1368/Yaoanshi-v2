package com.medicine.domain.medication;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class MedicationPlan extends BaseEntity {

    private Long patientId;
    private String planName;
    private Integer versionNo;
    private String status;
    private String sourceType;
    private Long sourcePrescriptionId;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private String notes;
    private Long createdByPharmacistId;
    private LocalDateTime confirmedAt;
    private String patientName;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
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

    public Long getSourcePrescriptionId() {
        return sourcePrescriptionId;
    }

    public void setSourcePrescriptionId(Long sourcePrescriptionId) {
        this.sourcePrescriptionId = sourcePrescriptionId;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getCreatedByPharmacistId() {
        return createdByPharmacistId;
    }

    public void setCreatedByPharmacistId(Long createdByPharmacistId) {
        this.createdByPharmacistId = createdByPharmacistId;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
}
