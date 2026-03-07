package com.medicine.api.dto.medication;

public class CheckinSubmitRequest {

    private Long doseTaskId;
    private String result;
    private String actualTakenAt;
    private String note;
    private String skipReason;

    public Long getDoseTaskId() {
        return doseTaskId;
    }

    public void setDoseTaskId(Long doseTaskId) {
        this.doseTaskId = doseTaskId;
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
}
