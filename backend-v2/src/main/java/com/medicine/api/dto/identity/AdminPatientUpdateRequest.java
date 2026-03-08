package com.medicine.api.dto.identity;

import java.util.Map;

public class AdminPatientUpdateRequest {

    private String name;
    private String gender;
    private String birthDate;
    private Integer age;
    private String primaryDiseaseCode;
    private String primaryDiseaseName;
    private String drugAllergy;
    private String foodAllergy;
    private Map<String, Object> medicalHistory;
    private Abcd2InputDTO abcd2Input;

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

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
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

    public Map<String, Object> getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(Map<String, Object> medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public Abcd2InputDTO getAbcd2Input() {
        return abcd2Input;
    }

    public void setAbcd2Input(Abcd2InputDTO abcd2Input) {
        this.abcd2Input = abcd2Input;
    }
}
