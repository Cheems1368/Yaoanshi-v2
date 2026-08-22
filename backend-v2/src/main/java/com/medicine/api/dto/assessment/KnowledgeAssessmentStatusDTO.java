package com.medicine.api.dto.assessment;

public class KnowledgeAssessmentStatusDTO {
    private String questionSetVersion;
    private String planStartDate;
    private String currentPhase;
    private boolean due;
    private KnowledgeAssessmentDTO day1;
    private KnowledgeAssessmentDTO week4;

    public String getQuestionSetVersion() { return questionSetVersion; }
    public void setQuestionSetVersion(String questionSetVersion) { this.questionSetVersion = questionSetVersion; }
    public String getPlanStartDate() { return planStartDate; }
    public void setPlanStartDate(String planStartDate) { this.planStartDate = planStartDate; }
    public String getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(String currentPhase) { this.currentPhase = currentPhase; }
    public boolean isDue() { return due; }
    public void setDue(boolean due) { this.due = due; }
    public KnowledgeAssessmentDTO getDay1() { return day1; }
    public void setDay1(KnowledgeAssessmentDTO day1) { this.day1 = day1; }
    public KnowledgeAssessmentDTO getWeek4() { return week4; }
    public void setWeek4(KnowledgeAssessmentDTO week4) { this.week4 = week4; }
}
