package com.medicine.api.dto.helporder;

public class HelpOrderEscalateRequest {

    private Long doctorId;
    private String note;

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
