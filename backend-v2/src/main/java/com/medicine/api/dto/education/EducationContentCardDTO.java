package com.medicine.api.dto.education;

import java.util.List;

public class EducationContentCardDTO {

    private Long id;
    private String contentCode;
    private String title;
    private String summary;
    private String body;
    private String category;
    private String categoryName;
    private List<String> diagnosisTags;
    private List<String> drugTags;
    private List<String> populationTags;
    private String source;
    private String reviewStatus;
    private String reviewedBy;
    private String reviewedAt;
    private String updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public List<String> getDiagnosisTags() { return diagnosisTags; }
    public void setDiagnosisTags(List<String> diagnosisTags) { this.diagnosisTags = diagnosisTags; }
    public List<String> getDrugTags() { return drugTags; }
    public void setDrugTags(List<String> drugTags) { this.drugTags = drugTags; }
    public List<String> getPopulationTags() { return populationTags; }
    public void setPopulationTags(List<String> populationTags) { this.populationTags = populationTags; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
