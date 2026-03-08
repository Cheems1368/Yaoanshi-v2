package com.medicine.domain.identity;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class PatientHealthProfile extends BaseEntity {

    private Long patientId;
    private String layer1Allergy;
    private String layer1Diagnosis;
    private String layer2MedicalHistory;
    private String layer2SmokingDrinking;
    private String layer2LiverKidney;
    private String layer2CurrentMedication;
    private String layer3DetailedSymptoms;
    private String layer3FamilyHistory;
    private String layer3SurgeryHistory;
    private String layer3SocialHistory;
    private LocalDateTime submittedAt;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getLayer1Allergy() {
        return layer1Allergy;
    }

    public void setLayer1Allergy(String layer1Allergy) {
        this.layer1Allergy = layer1Allergy;
    }

    public String getLayer1Diagnosis() {
        return layer1Diagnosis;
    }

    public void setLayer1Diagnosis(String layer1Diagnosis) {
        this.layer1Diagnosis = layer1Diagnosis;
    }

    public String getLayer2MedicalHistory() {
        return layer2MedicalHistory;
    }

    public void setLayer2MedicalHistory(String layer2MedicalHistory) {
        this.layer2MedicalHistory = layer2MedicalHistory;
    }

    public String getLayer2SmokingDrinking() {
        return layer2SmokingDrinking;
    }

    public void setLayer2SmokingDrinking(String layer2SmokingDrinking) {
        this.layer2SmokingDrinking = layer2SmokingDrinking;
    }

    public String getLayer2LiverKidney() {
        return layer2LiverKidney;
    }

    public void setLayer2LiverKidney(String layer2LiverKidney) {
        this.layer2LiverKidney = layer2LiverKidney;
    }

    public String getLayer2CurrentMedication() {
        return layer2CurrentMedication;
    }

    public void setLayer2CurrentMedication(String layer2CurrentMedication) {
        this.layer2CurrentMedication = layer2CurrentMedication;
    }

    public String getLayer3DetailedSymptoms() {
        return layer3DetailedSymptoms;
    }

    public void setLayer3DetailedSymptoms(String layer3DetailedSymptoms) {
        this.layer3DetailedSymptoms = layer3DetailedSymptoms;
    }

    public String getLayer3FamilyHistory() {
        return layer3FamilyHistory;
    }

    public void setLayer3FamilyHistory(String layer3FamilyHistory) {
        this.layer3FamilyHistory = layer3FamilyHistory;
    }

    public String getLayer3SurgeryHistory() {
        return layer3SurgeryHistory;
    }

    public void setLayer3SurgeryHistory(String layer3SurgeryHistory) {
        this.layer3SurgeryHistory = layer3SurgeryHistory;
    }

    public String getLayer3SocialHistory() {
        return layer3SocialHistory;
    }

    public void setLayer3SocialHistory(String layer3SocialHistory) {
        this.layer3SocialHistory = layer3SocialHistory;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
