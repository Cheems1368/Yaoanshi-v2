package com.medicine.api.dto.identity;

public class AppPatientHealthProfileUpdateRequest {

    private AppPatientHealthProfileDTO.Layer1DTO layer1;
    private AppPatientHealthProfileDTO.Layer2DTO layer2;
    private AppPatientHealthProfileDTO.Layer3DTO layer3;

    public AppPatientHealthProfileDTO.Layer1DTO getLayer1() {
        return layer1;
    }

    public void setLayer1(AppPatientHealthProfileDTO.Layer1DTO layer1) {
        this.layer1 = layer1;
    }

    public AppPatientHealthProfileDTO.Layer2DTO getLayer2() {
        return layer2;
    }

    public void setLayer2(AppPatientHealthProfileDTO.Layer2DTO layer2) {
        this.layer2 = layer2;
    }

    public AppPatientHealthProfileDTO.Layer3DTO getLayer3() {
        return layer3;
    }

    public void setLayer3(AppPatientHealthProfileDTO.Layer3DTO layer3) {
        this.layer3 = layer3;
    }
}
