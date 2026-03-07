package com.medicine.domain.identity;

public class PatientAdminListRow {

    private Long id;
    private String name;
    private String mobile;
    private String hospitalPatientId;
    private String bindingStatus;
    private String primaryDiseaseName;
    private String gender;
    private Integer age;
    private String drugAllergy;
    private String foodAllergy;
    private String medicalHistoryJson;
    private Long boundByPharmacistId;
    private String responsiblePharmacistName;
    private Integer familyBindingCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getHospitalPatientId() {
        return hospitalPatientId;
    }

    public void setHospitalPatientId(String hospitalPatientId) {
        this.hospitalPatientId = hospitalPatientId;
    }

    public String getBindingStatus() {
        return bindingStatus;
    }

    public void setBindingStatus(String bindingStatus) {
        this.bindingStatus = bindingStatus;
    }

    public String getPrimaryDiseaseName() {
        return primaryDiseaseName;
    }

    public void setPrimaryDiseaseName(String primaryDiseaseName) {
        this.primaryDiseaseName = primaryDiseaseName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
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

    public Long getBoundByPharmacistId() {
        return boundByPharmacistId;
    }

    public void setBoundByPharmacistId(Long boundByPharmacistId) {
        this.boundByPharmacistId = boundByPharmacistId;
    }

    public String getResponsiblePharmacistName() {
        return responsiblePharmacistName;
    }

    public void setResponsiblePharmacistName(String responsiblePharmacistName) {
        this.responsiblePharmacistName = responsiblePharmacistName;
    }

    public Integer getFamilyBindingCount() {
        return familyBindingCount;
    }

    public void setFamilyBindingCount(Integer familyBindingCount) {
        this.familyBindingCount = familyBindingCount;
    }
}
