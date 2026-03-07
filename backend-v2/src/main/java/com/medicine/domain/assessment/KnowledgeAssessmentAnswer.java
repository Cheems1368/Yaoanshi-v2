package com.medicine.domain.assessment;

import com.medicine.domain.common.BaseEntity;

public class KnowledgeAssessmentAnswer extends BaseEntity {

    private Long assessmentId;
    private String questionCode;
    private String questionTextSnapshot;
    private String selectedOption;
    private String correctOption;
    private Boolean isCorrect;

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getQuestionCode() {
        return questionCode;
    }

    public void setQuestionCode(String questionCode) {
        this.questionCode = questionCode;
    }

    public String getQuestionTextSnapshot() {
        return questionTextSnapshot;
    }

    public void setQuestionTextSnapshot(String questionTextSnapshot) {
        this.questionTextSnapshot = questionTextSnapshot;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean correct) {
        isCorrect = correct;
    }
}
