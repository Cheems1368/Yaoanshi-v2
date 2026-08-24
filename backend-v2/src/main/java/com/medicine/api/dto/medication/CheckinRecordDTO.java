package com.medicine.api.dto.medication;

public class CheckinRecordDTO {

    private Long id;
    private Long doseTaskId;
    private Long patientId;
    private String clientRequestId;
    private String operatorType;
    private Long operatorId;
    private String result;
    private String actualTakenAt;
    private String note;
    private String skipReason;
    private Boolean correction;
    private Long supersedesRecordId;
    private Boolean effective;
    private String createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getClientRequestId() {
        return clientRequestId;
    }

    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
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

    public String getActualTakenAt() {
        return actualTakenAt;
    }

    public void setActualTakenAt(String actualTakenAt) {
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

    public Boolean getCorrection() {
        return correction;
    }

    public void setCorrection(Boolean correction) {
        this.correction = correction;
    }

    public Long getSupersedesRecordId() {
        return supersedesRecordId;
    }

    public void setSupersedesRecordId(Long supersedesRecordId) {
        this.supersedesRecordId = supersedesRecordId;
    }

    public Boolean getEffective() {
        return effective;
    }

    public void setEffective(Boolean effective) {
        this.effective = effective;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
