package com.medicine.api.dto.assessment;

import java.util.List;

public class KnowledgeAssessmentSubmitRequest {
    private String phase;
    private String questionSetVersion;
    private List<AnswerRequest> answers;

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public String getQuestionSetVersion() { return questionSetVersion; }
    public void setQuestionSetVersion(String questionSetVersion) { this.questionSetVersion = questionSetVersion; }
    public List<AnswerRequest> getAnswers() { return answers; }
    public void setAnswers(List<AnswerRequest> answers) { this.answers = answers; }

    public static class AnswerRequest {
        private String questionCode;
        private String selectedOption;
        public String getQuestionCode() { return questionCode; }
        public void setQuestionCode(String questionCode) { this.questionCode = questionCode; }
        public String getSelectedOption() { return selectedOption; }
        public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
    }
}
