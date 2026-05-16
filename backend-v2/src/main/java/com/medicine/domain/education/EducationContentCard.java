package com.medicine.domain.education;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class EducationContentCard extends BaseEntity {

    private String contentCode;
    private String title;
    private String summary;
    private String body;
    private String category;
    private String categoryName;
    private String diagnosisTags;
    private String drugTags;
    private String populationTags;
    private String source;
    private String reviewStatus;
    private String reviewedBy;
    private LocalDateTime reviewedAt;

    public String getContentCode() { return contentCode; }
    public void setContentCode(String contentCode) { this.contentCode = contentCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getDiagnosisTags() { return diagnosisTags; }
    public void setDiagnosisTags(String diagnosisTags) { this.diagnosisTags = diagnosisTags; }
    public String getDrugTags() { return drugTags; }
    public void setDrugTags(String drugTags) { this.drugTags = drugTags; }
    public String getPopulationTags() { return populationTags; }
    public void setPopulationTags(String populationTags) { this.populationTags = populationTags; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
