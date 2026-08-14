package com.medicine.api.dto.ddi;

public class AdminDdiRiskCardDTO {

    private String cardKey;
    private Long ruleId;
    private String ruleCode;
    private Long screeningId;
    private String alertType;
    private String severity;
    private String drugA;
    private String drugB;
    private String conditionType;
    private String conditionValue;
    private String message;
    private String recommendedAction;
    private String mechanismDesc;
    private String alternativeRecommendation;
    private String referenceSource;
    private String patientSummary;
    private String confirmStatus;
    private String confirmationNote;

    public String getCardKey() {
        return cardKey;
    }

    public void setCardKey(String cardKey) {
        this.cardKey = cardKey;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public Long getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(Long screeningId) {
        this.screeningId = screeningId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDrugA() {
        return drugA;
    }

    public void setDrugA(String drugA) {
        this.drugA = drugA;
    }

    public String getDrugB() {
        return drugB;
    }

    public void setDrugB(String drugB) {
        this.drugB = drugB;
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

    public String getConfirmStatus() {
        return confirmStatus;
    }

    public void setConfirmStatus(String confirmStatus) {
        this.confirmStatus = confirmStatus;
    }

    public String getConfirmationNote() {
        return confirmationNote;
    }

    public void setConfirmationNote(String confirmationNote) {
        this.confirmationNote = confirmationNote;
    }
}
