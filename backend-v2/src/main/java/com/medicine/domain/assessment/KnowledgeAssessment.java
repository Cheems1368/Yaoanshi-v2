package com.medicine.domain.assessment;

import com.medicine.domain.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class KnowledgeAssessment extends BaseEntity {

    private Long patientId;
    private String phase;
    private String questionSetVersion;
    private BigDecimal score;
    private BigDecimal maxScore;
    private String submittedByType;
    private Long submittedById;
    private LocalDateTime submittedAt;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getQuestionSetVersion() {
        return questionSetVersion;
    }

    public void setQuestionSetVersion(String questionSetVersion) {
        this.questionSetVersion = questionSetVersion;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public String getSubmittedByType() {
        return submittedByType;
    }

    public void setSubmittedByType(String submittedByType) {
        this.submittedByType = submittedByType;
    }

    public Long getSubmittedById() {
        return submittedById;
    }

    public void setSubmittedById(Long submittedById) {
        this.submittedById = submittedById;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
