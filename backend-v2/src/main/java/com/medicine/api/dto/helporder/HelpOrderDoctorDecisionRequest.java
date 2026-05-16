package com.medicine.api.dto.helporder;

public class HelpOrderDoctorDecisionRequest {

    private String decisionResult;
    private String content;

    public String getDecisionResult() {
        return decisionResult;
    }

    public void setDecisionResult(String decisionResult) {
        this.decisionResult = decisionResult;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
