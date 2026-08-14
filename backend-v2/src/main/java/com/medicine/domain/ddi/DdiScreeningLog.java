package com.medicine.domain.ddi;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class DdiScreeningLog extends BaseEntity {

    private Long patientId;
    private Long prescriptionId;
    private Long medicationPlanId;
    private Long matchedRuleId;
    private String ruleCode;
    private String ruleStatusAtScreening;
    private String drugAName;
    private String drugBName;
    private String severity;
    private String message;
    private String recommendedAction;
    private String mechanismDesc;
    private String alternativeRecommendation;
    private String referenceSource;
    private String patientSummary;
    private String confirmStatus;
    private Long confirmedByPharmacistId;
    private LocalDateTime confirmedAt;
    private String confirmationNote;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public Long getMedicationPlanId() {
        return medicationPlanId;
    }

    public void setMedicationPlanId(Long medicationPlanId) {
        this.medicationPlanId = medicationPlanId;
    }

    public Long getMatchedRuleId() {
        return matchedRuleId;
    }

    public void setMatchedRuleId(Long matchedRuleId) {
        this.matchedRuleId = matchedRuleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleStatusAtScreening() {
        return ruleStatusAtScreening;
    }

    public void setRuleStatusAtScreening(String ruleStatusAtScreening) {
        this.ruleStatusAtScreening = ruleStatusAtScreening;
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

    public Long getConfirmedByPharmacistId() {
        return confirmedByPharmacistId;
    }

    public void setConfirmedByPharmacistId(Long confirmedByPharmacistId) {
        this.confirmedByPharmacistId = confirmedByPharmacistId;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getConfirmationNote() {
        return confirmationNote;
    }

    public void setConfirmationNote(String confirmationNote) {
        this.confirmationNote = confirmationNote;
    }
}
