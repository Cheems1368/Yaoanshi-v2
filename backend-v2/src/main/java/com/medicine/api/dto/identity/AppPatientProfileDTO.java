package com.medicine.api.dto.identity;

public class AppPatientProfileDTO {

    private Long patientId;
    private String hospitalPatientId;
    private String name;
    private String phone;
    private Integer age;
    private String gender;
    private String idCard;
    private String idType;
    private String ethnicity;
    private Integer height;
    private Integer weight;
    private String bloodType;
    private String allergies;
    private String avatar;
    private String address;
    private Integer patientType;
    private Integer insurance;
    private EmergencyContactDTO emergencyContact = new EmergencyContactDTO();
    private Boolean identityMaterialArchived;
    private Boolean realNameVerified;
    private String realNameVerifiedTime;
    private String createdAt;
    private String updatedAt;
    private String operatorType;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getHospitalPatientId() {
        return hospitalPatientId;
    }

    public void setHospitalPatientId(String hospitalPatientId) {
        this.hospitalPatientId = hospitalPatientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPatientType() {
        return patientType;
    }

    public void setPatientType(Integer patientType) {
        this.patientType = patientType;
    }

    public Integer getInsurance() {
        return insurance;
    }

    public void setInsurance(Integer insurance) {
        this.insurance = insurance;
    }

    public EmergencyContactDTO getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(EmergencyContactDTO emergencyContact) {
        this.emergencyContact = emergencyContact == null ? new EmergencyContactDTO() : emergencyContact;
    }

    public Boolean getIdentityMaterialArchived() {
        return identityMaterialArchived;
    }

    public void setIdentityMaterialArchived(Boolean identityMaterialArchived) {
        this.identityMaterialArchived = identityMaterialArchived;
    }

    public Boolean getRealNameVerified() {
        return realNameVerified;
    }

    public void setRealNameVerified(Boolean realNameVerified) {
        this.realNameVerified = realNameVerified;
    }

    public String getRealNameVerifiedTime() {
        return realNameVerifiedTime;
    }

    public void setRealNameVerifiedTime(String realNameVerifiedTime) {
        this.realNameVerifiedTime = realNameVerifiedTime;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(String operatorType) {
        this.operatorType = operatorType;
    }

    public static class EmergencyContactDTO {
        private String name;
        private String phone;
        private String relationship;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
            this.relationship = relationship;
        }
    }
}
