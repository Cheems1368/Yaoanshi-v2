package com.medicine.domain.medication;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class MedicationCheckinRecord extends BaseEntity {

    private Long doseTaskId;
    private Long patientId;
    private String operatorType;
    private Long operatorId;
    private String result;
    private LocalDateTime actualTakenAt;
    private String note;
    private String skipReason;
    private Boolean isCorrection;
    private Long supersedesRecordId;
    private Boolean isEffective;

    public Long getDoseTaskId() {
        return doseTaskId;
    }

    public void setDoseTaskId(Long doseTaskId) {
        this.doseTaskId = doseTaskId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LocalDateTime getActualTakenAt() {
        return actualTakenAt;
    }

    public void setActualTakenAt(LocalDateTime actualTakenAt) {
        this.actualTakenAt = actualTakenAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getSkipReason() {
        return skipReason;
    }

    public void setSkipReason(String skipReason) {
        this.skipReason = skipReason;
    }

    public Boolean getIsCorrection() {
        return isCorrection;
    }

    public void setIsCorrection(Boolean correction) {
        isCorrection = correction;
    }

    public Long getSupersedesRecordId() {
        return supersedesRecordId;
    }

    public void setSupersedesRecordId(Long supersedesRecordId) {
        this.supersedesRecordId = supersedesRecordId;
    }

    public Boolean getIsEffective() {
        return isEffective;
    }

    public void setIsEffective(Boolean effective) {
        isEffective = effective;
    }
}
