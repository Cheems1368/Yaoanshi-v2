package com.medicine.api.dto.assessment;

public class SatisfactionStatusDTO {
    private Long helpOrderId;
    private boolean eligible;
    private boolean submitted;
    private SatisfactionSurveyDTO survey;

    public Long getHelpOrderId() { return helpOrderId; }
    public void setHelpOrderId(Long helpOrderId) { this.helpOrderId = helpOrderId; }
    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }
    public boolean isSubmitted() { return submitted; }
    public void setSubmitted(boolean submitted) { this.submitted = submitted; }
    public SatisfactionSurveyDTO getSurvey() { return survey; }
    public void setSurvey(SatisfactionSurveyDTO survey) { this.survey = survey; }
}
