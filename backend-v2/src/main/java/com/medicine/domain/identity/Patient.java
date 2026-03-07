package com.medicine.domain.identity;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Patient extends BaseEntity {

    private Long appAccountId;
    private String hospitalPatientId;
    private String mobile;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private Integer age;
    private String idCardMasked;
    private String primaryDiseaseCode;
    private String primaryDiseaseName;
    private String drugAllergy;
    private String foodAllergy;
    private String medicalHistoryJson;
    private String bindingStatus;
    private Long boundByPharmacistId;
    private LocalDateTime boundAt;

    public Long getAppAccountId() {
        return appAccountId;
    }

    public void setAppAccountId(Long appAccountId) {
        this.appAccountId = appAccountId;
    }

    public String getHospitalPatientId() {
        return hospitalPatientId;
    }

    public void setHospitalPatientId(String hospitalPatientId) {
        this.hospitalPatientId = hospitalPatientId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getIdCardMasked() {
        return idCardMasked;
    }

    public void setIdCardMasked(String idCardMasked) {
        this.idCardMasked = idCardMasked;
    }

    public String getPrimaryDiseaseCode() {
        return primaryDiseaseCode;
    }

    public void setPrimaryDiseaseCode(String primaryDiseaseCode) {
        this.primaryDiseaseCode = primaryDiseaseCode;
    }

    public String getPrimaryDiseaseName() {
        return primaryDiseaseName;
    }

    public void setPrimaryDiseaseName(String primaryDiseaseName) {
        this.primaryDiseaseName = primaryDiseaseName;
    }

    public String getDrugAllergy() {
        return drugAllergy;
    }

    public void setDrugAllergy(String drugAllergy) {
        this.drugAllergy = drugAllergy;
    }

    public String getFoodAllergy() {
        return foodAllergy;
    }

    public void setFoodAllergy(String foodAllergy) {
        this.foodAllergy = foodAllergy;
    }

    public String getMedicalHistoryJson() {
        return medicalHistoryJson;
    }

    public void setMedicalHistoryJson(String medicalHistoryJson) {
        this.medicalHistoryJson = medicalHistoryJson;
    }

    public String getBindingStatus() {
        return bindingStatus;
    }

    public void setBindingStatus(String bindingStatus) {
        this.bindingStatus = bindingStatus;
    }

    public Long getBoundByPharmacistId() {
        return boundByPharmacistId;
    }

    public void setBoundByPharmacistId(Long boundByPharmacistId) {
        this.boundByPharmacistId = boundByPharmacistId;
    }

    public LocalDateTime getBoundAt() {
        return boundAt;
    }

    public void setBoundAt(LocalDateTime boundAt) {
        this.boundAt = boundAt;
    }
}
