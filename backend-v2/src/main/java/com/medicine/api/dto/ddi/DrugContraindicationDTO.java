package com.medicine.api.dto.ddi;

public class DrugContraindicationDTO {

    private Long id;
    private String ruleCode;
    private String drugName;
    private String conditionType;
    private String conditionValue;
    private String severity;
    private String message;
    private String recommendedAction;
    private String mechanismDesc;
    private String alternativeRecommendation;
    private String referenceSource;
    private String patientSummary;
    private String status;
    private Long maintainedByPharmacistId;
    private String updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public String getMechanismDesc() {
        return mechanismDesc;
    }

    public void setMechanismDesc(String mechanismDesc) {
        this.mechanismDesc = mechanismDesc;
    }

    public String getAlternativeRecommendation() {
        return alternativeRecommendation;
    }

    public void setAlternativeRecommendation(String alternativeRecommendation) {
        this.alternativeRecommendation = alternativeRecommendation;
    }

    public String getReferenceSource() {
        return referenceSource;
    }

    public void setReferenceSource(String referenceSource) {
        this.referenceSource = referenceSource;
    }

    public String getPatientSummary() {
        return patientSummary;
    }

    public void setPatientSummary(String patientSummary) {
        this.patientSummary = patientSummary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getMaintainedByPharmacistId() {
        return maintainedByPharmacistId;
    }

    public void setMaintainedByPharmacistId(Long maintainedByPharmacistId) {
        this.maintainedByPharmacistId = maintainedByPharmacistId;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
