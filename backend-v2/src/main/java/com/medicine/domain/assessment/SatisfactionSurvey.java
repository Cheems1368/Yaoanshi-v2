package com.medicine.domain.assessment;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class SatisfactionSurvey extends BaseEntity {

    private Long helpOrderId;
    private Long patientId;
    private Integer rating;
    private String comment;
    private String submittedByType;
    private Long submittedById;
    private LocalDateTime submittedAt;

    public Long getHelpOrderId() {
        return helpOrderId;
    }

    public void setHelpOrderId(Long helpOrderId) {
        this.helpOrderId = helpOrderId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
