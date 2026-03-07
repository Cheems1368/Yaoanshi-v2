package com.medicine.domain.assessment;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class Abcd2Assessment extends BaseEntity {

    private Long patientId;
    private String sourceType;
    private Long sourceRefId;
    private String rawInputJson;
    private Integer score;
    private String riskLevel;
    private LocalDateTime calculatedAt;
    private Long calculatedById;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceRefId() {
        return sourceRefId;
    }

    public void setSourceRefId(Long sourceRefId) {
        this.sourceRefId = sourceRefId;
    }

    public String getRawInputJson() {
        return rawInputJson;
    }

    public void setRawInputJson(String rawInputJson) {
        this.rawInputJson = rawInputJson;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public Long getCalculatedById() {
        return calculatedById;
    }

    public void setCalculatedById(Long calculatedById) {
        this.calculatedById = calculatedById;
    }
}
