package com.medicine.api.dto.identity;

public class Abcd2InputDTO {

    private Boolean bloodPressureHigh;
    private String clinicalFeature;
    private Integer symptomDurationMinutes;
    private Boolean diabetes;

    public Boolean getBloodPressureHigh() {
        return bloodPressureHigh;
    }

    public void setBloodPressureHigh(Boolean bloodPressureHigh) {
        this.bloodPressureHigh = bloodPressureHigh;
    }

    public String getClinicalFeature() {
        return clinicalFeature;
    }

    public void setClinicalFeature(String clinicalFeature) {
        this.clinicalFeature = clinicalFeature;
    }

    public Integer getSymptomDurationMinutes() {
        return symptomDurationMinutes;
    }

    public void setSymptomDurationMinutes(Integer symptomDurationMinutes) {
        this.symptomDurationMinutes = symptomDurationMinutes;
    }

    public Boolean getDiabetes() {
        return diabetes;
    }

    public void setDiabetes(Boolean diabetes) {
        this.diabetes = diabetes;
    }
}
