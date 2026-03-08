package com.medicine.api.dto.identity;

public class AppPatientHealthProfileDTO {

    private Long patientId;
    private Layer1DTO layer1 = new Layer1DTO();
    private Layer2DTO layer2 = new Layer2DTO();
    private Layer3DTO layer3 = new Layer3DTO();
    private String submitTime;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Layer1DTO getLayer1() {
        return layer1;
    }

    public void setLayer1(Layer1DTO layer1) {
        this.layer1 = layer1 == null ? new Layer1DTO() : layer1;
    }

    public Layer2DTO getLayer2() {
        return layer2;
    }

    public void setLayer2(Layer2DTO layer2) {
        this.layer2 = layer2 == null ? new Layer2DTO() : layer2;
    }

    public Layer3DTO getLayer3() {
        return layer3;
    }

    public void setLayer3(Layer3DTO layer3) {
        this.layer3 = layer3 == null ? new Layer3DTO() : layer3;
    }

    public String getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(String submitTime) {
        this.submitTime = submitTime;
    }

    public static class Layer1DTO {
        private String allergy;
        private String diagnosis;

        public String getAllergy() {
            return allergy;
        }

        public void setAllergy(String allergy) {
            this.allergy = allergy;
        }

        public String getDiagnosis() {
            return diagnosis;
        }

        public void setDiagnosis(String diagnosis) {
            this.diagnosis = diagnosis;
        }
    }

    public static class Layer2DTO {
        private String medicalHistory;
        private String smokingDrinking;
        private String liverKidney;
        private String currentMedication;

        public String getMedicalHistory() {
            return medicalHistory;
        }

        public void setMedicalHistory(String medicalHistory) {
            this.medicalHistory = medicalHistory;
        }

        public String getSmokingDrinking() {
            return smokingDrinking;
        }

        public void setSmokingDrinking(String smokingDrinking) {
            this.smokingDrinking = smokingDrinking;
        }

        public String getLiverKidney() {
            return liverKidney;
        }

        public void setLiverKidney(String liverKidney) {
            this.liverKidney = liverKidney;
        }

        public String getCurrentMedication() {
            return currentMedication;
        }

        public void setCurrentMedication(String currentMedication) {
            this.currentMedication = currentMedication;
        }
    }

    public static class Layer3DTO {
        private String detailedSymptoms;
        private String familyHistory;
        private String surgeryHistory;
        private String socialHistory;

        public String getDetailedSymptoms() {
            return detailedSymptoms;
        }

        public void setDetailedSymptoms(String detailedSymptoms) {
            this.detailedSymptoms = detailedSymptoms;
        }

        public String getFamilyHistory() {
            return familyHistory;
        }

        public void setFamilyHistory(String familyHistory) {
            this.familyHistory = familyHistory;
        }

        public String getSurgeryHistory() {
            return surgeryHistory;
        }

        public void setSurgeryHistory(String surgeryHistory) {
            this.surgeryHistory = surgeryHistory;
        }

        public String getSocialHistory() {
            return socialHistory;
        }

        public void setSocialHistory(String socialHistory) {
            this.socialHistory = socialHistory;
        }
    }
}
