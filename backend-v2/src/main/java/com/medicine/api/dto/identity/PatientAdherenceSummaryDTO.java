package com.medicine.api.dto.identity;

public class PatientAdherenceSummaryDTO {

    private String status;
    private Double sevenDayAdherence;
    private Double thirtyDayAdherence;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getSevenDayAdherence() {
        return sevenDayAdherence;
    }

    public void setSevenDayAdherence(Double sevenDayAdherence) {
        this.sevenDayAdherence = sevenDayAdherence;
    }

    public Double getThirtyDayAdherence() {
        return thirtyDayAdherence;
    }

    public void setThirtyDayAdherence(Double thirtyDayAdherence) {
        this.thirtyDayAdherence = thirtyDayAdherence;
    }
}
