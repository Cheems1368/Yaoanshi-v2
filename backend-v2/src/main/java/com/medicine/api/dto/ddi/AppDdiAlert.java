package com.medicine.api.dto.ddi;

public class AppDdiAlert {

    private String type;
    private String severity;
    private String drugA;
    private String drugB;
    private String message;
    private String recommendedAction;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
