package com.medicine.api.dto.ddi;

public class AdminDdiRuleDTO {

    private Long id;
    private String ruleCode;
    private String drugAName;
    private String drugBName;
    private String severity;
    private String message;
    private String recommendedAction;
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
