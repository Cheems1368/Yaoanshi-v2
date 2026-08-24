package com.medicine.api.dto.identity;

import com.medicine.api.dto.assessment.KnowledgeAssessmentDTO;

import java.util.List;
import java.util.Map;

public class AdminPatientDetailDTO {

    private Long id;
    private String name;
    private String mobileMasked;
    private String hospitalPatientId;
    private String bindingStatus;
    private String boundAt;
    private Long responsiblePharmacistId;
    private String responsiblePharmacistName;
    private String gender;
    private String birthDate;
    private Integer age;
    private String primaryDiseaseCode;
    private String primaryDiseaseName;
    private String drugAllergy;
    private String foodAllergy;
    private Map<String, Object> medicalHistory;
    private Abcd2InputDTO abcd2Input;
    private List<String> pendingFields;
    private List<FamilyBindingSummaryDTO> familyBindings;
    private Abcd2AssessmentDTO latestAbcd2Assessment;
    private PatientAdherenceSummaryDTO adherenceSummary;
    private Long latestMedicationPlanId;
    private String latestMedicationPlanName;
    private String latestMedicationPlanStatus;
    private Integer latestMedicationPlanVersionNo;
    private String latestMedicationPlanConfirmedAt;
    private KnowledgeAssessmentDTO day1KnowledgeAssessment;
    private KnowledgeAssessmentDTO week4KnowledgeAssessment;
    private long satisfactionCount;
    private Double satisfactionAverage;

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

    public String getMobileMasked() {
        return mobileMasked;
    }

    public void setMobileMasked(String mobileMasked) {
        this.mobileMasked = mobileMasked;
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

    public String getBoundAt() {
        return boundAt;
    }

    public void setBoundAt(String boundAt) {
        this.boundAt = boundAt;
    }

    public Long getResponsiblePharmacistId() {
        return responsiblePharmacistId;
    }

    public void setResponsiblePharmacistId(Long responsiblePharmacistId) {
        this.responsiblePharmacistId = responsiblePharmacistId;
    }

    public String getResponsiblePharmacistName() {
        return responsiblePharmacistName;
    }

    public void setResponsiblePharmacistName(String responsiblePharmacistName) {
        this.responsiblePharmacistName = responsiblePharmacistName;
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

    public List<String> getPendingFields() {
        return pendingFields;
    }

    public void setPendingFields(List<String> pendingFields) {
        this.pendingFields = pendingFields;
    }

    public List<FamilyBindingSummaryDTO> getFamilyBindings() {
        return familyBindings;
    }

    public void setFamilyBindings(List<FamilyBindingSummaryDTO> familyBindings) {
        this.familyBindings = familyBindings;
    }

    public Abcd2AssessmentDTO getLatestAbcd2Assessment() {
        return latestAbcd2Assessment;
    }

    public void setLatestAbcd2Assessment(Abcd2AssessmentDTO latestAbcd2Assessment) {
        this.latestAbcd2Assessment = latestAbcd2Assessment;
    }

    public PatientAdherenceSummaryDTO getAdherenceSummary() {
        return adherenceSummary;
    }

    public void setAdherenceSummary(PatientAdherenceSummaryDTO adherenceSummary) {
        this.adherenceSummary = adherenceSummary;
    }

    public Long getLatestMedicationPlanId() {
        return latestMedicationPlanId;
    }

    public void setLatestMedicationPlanId(Long latestMedicationPlanId) {
        this.latestMedicationPlanId = latestMedicationPlanId;
    }

    public String getLatestMedicationPlanName() {
        return latestMedicationPlanName;
    }

    public void setLatestMedicationPlanName(String latestMedicationPlanName) {
        this.latestMedicationPlanName = latestMedicationPlanName;
    }

    public String getLatestMedicationPlanStatus() {
        return latestMedicationPlanStatus;
    }

    public void setLatestMedicationPlanStatus(String latestMedicationPlanStatus) {
        this.latestMedicationPlanStatus = latestMedicationPlanStatus;
    }

    public Integer getLatestMedicationPlanVersionNo() {
        return latestMedicationPlanVersionNo;
    }

    public void setLatestMedicationPlanVersionNo(Integer latestMedicationPlanVersionNo) {
        this.latestMedicationPlanVersionNo = latestMedicationPlanVersionNo;
    }

    public String getLatestMedicationPlanConfirmedAt() {
        return latestMedicationPlanConfirmedAt;
    }

    public void setLatestMedicationPlanConfirmedAt(String latestMedicationPlanConfirmedAt) {
        this.latestMedicationPlanConfirmedAt = latestMedicationPlanConfirmedAt;
    }

    public KnowledgeAssessmentDTO getDay1KnowledgeAssessment() { return day1KnowledgeAssessment; }
    public void setDay1KnowledgeAssessment(KnowledgeAssessmentDTO value) { this.day1KnowledgeAssessment = value; }
    public KnowledgeAssessmentDTO getWeek4KnowledgeAssessment() { return week4KnowledgeAssessment; }
    public void setWeek4KnowledgeAssessment(KnowledgeAssessmentDTO value) { this.week4KnowledgeAssessment = value; }
    public long getSatisfactionCount() { return satisfactionCount; }
    public void setSatisfactionCount(long satisfactionCount) { this.satisfactionCount = satisfactionCount; }
    public Double getSatisfactionAverage() { return satisfactionAverage; }
    public void setSatisfactionAverage(Double satisfactionAverage) { this.satisfactionAverage = satisfactionAverage; }
}
