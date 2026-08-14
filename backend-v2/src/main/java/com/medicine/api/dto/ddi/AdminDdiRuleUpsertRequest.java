package com.medicine.api.dto.ddi;

public class AdminDdiRuleUpsertRequest {

    private String ruleCode;
    private String drugAName;
    private String drugBName;
    private String severity;
    private String message;
    private String recommendedAction;
    private String status;
    private String mechanismDesc;
    private String alternativeRecommendation;
    private String referenceSource;
    private String patientSummary;

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getDrugAName() {
        return drugAName;
    }

    public void setDrugAName(String drugAName) {
        this.drugAName = drugAName;
    }

    public String getDrugBName() {
        return drugBName;
    }

    public void setDrugBName(String drugBName) {
        this.drugBName = drugBName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
