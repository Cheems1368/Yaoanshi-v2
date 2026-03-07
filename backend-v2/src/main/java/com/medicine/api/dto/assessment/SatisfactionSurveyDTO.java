package com.medicine.api.dto.assessment;

public class SatisfactionSurveyDTO {

    private Long id;
    private Long helpOrderId;
    private Integer rating;
    private String comment;
    private String submittedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHelpOrderId() {
        return helpOrderId;
    }

    public void setHelpOrderId(Long helpOrderId) {
        this.helpOrderId = helpOrderId;
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

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }
}
