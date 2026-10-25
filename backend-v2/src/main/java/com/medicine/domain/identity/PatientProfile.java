package com.medicine.domain.identity;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class PatientProfile extends BaseEntity {

    private Long patientId;
    private String contactMobile;
    private String idType;
    private String ethnicity;
    private Integer heightCm;
    private Integer weightKg;
    private String bloodType;
    private String allergies;
    private String avatarUrl;
    private String address;
    private Integer patientType;
    private Integer insurance;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;
    private String identityMaterialHash;
    private Boolean realNameVerified = false;
    private LocalDateTime realNameVerifiedTime;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getContactMobile() {
        return contactMobile;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
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

    public Integer getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Integer heightCm) {
        this.heightCm = heightCm;
    }

    public Integer getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Integer weightKg) {
        this.weightKg = weightKg;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getEmergencyContactRelationship() {
        return emergencyContactRelationship;
    }

    public void setEmergencyContactRelationship(String emergencyContactRelationship) {
        this.emergencyContactRelationship = emergencyContactRelationship;
    }

    public String getIdentityMaterialHash() {
        return identityMaterialHash;
    }

    public void setIdentityMaterialHash(String identityMaterialHash) {
        this.identityMaterialHash = identityMaterialHash;
    }

    public Boolean getRealNameVerified() {
        return realNameVerified;
    }

    public void setRealNameVerified(Boolean realNameVerified) {
        this.realNameVerified = realNameVerified;
    }

    public LocalDateTime getRealNameVerifiedTime() {
        return realNameVerifiedTime;
    }

    public void setRealNameVerifiedTime(LocalDateTime realNameVerifiedTime) {
        this.realNameVerifiedTime = realNameVerifiedTime;
    }
}
