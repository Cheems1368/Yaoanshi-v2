package com.medicine.api.dto.identity;

import java.util.Map;

public class Abcd2AssessmentDTO {

    private Long id;
    private Integer score;
    private String riskLevel;
    private String calculatedAt;
    private Map<String, Object> rawInput;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(String calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public Map<String, Object> getRawInput() {
        return rawInput;
    }

    public void setRawInput(Map<String, Object> rawInput) {
        this.rawInput = rawInput;
    }
}
