package com.medicine.domain.ddi;

import com.medicine.domain.common.BaseEntity;

public class DrugContraindication extends BaseEntity {

    private String ruleCode;
    private String drugName;
    private String conditionType;
    private String conditionValue;
    private String severity;
    private String message;
    private String recommendedAction;
    private String status;
    private Long maintainedByPharmacistId;

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
}
