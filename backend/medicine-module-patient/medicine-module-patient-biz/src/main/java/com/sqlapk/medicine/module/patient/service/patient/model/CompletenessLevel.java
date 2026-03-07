package com.sqlapk.medicine.module.patient.service.patient.model;

/**
 * 患者档案完整性等级
 */
public enum CompletenessLevel {
    
    EXCELLENT("优秀", 90, 100),
    GOOD("良好", 80, 89),
    FAIR("一般", 70, 79),
    POOR("较差", 0, 69);
    
    private final String description;
    private final int minScore;
    private final int maxScore;
    
    CompletenessLevel(String description, int minScore, int maxScore) {
        this.description = description;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getMinScore() {
        return minScore;
    }
    
    public int getMaxScore() {
        return maxScore;
    }
    
    /**
     * 根据得分获取完整性等级
     */
    public static CompletenessLevel fromScore(double score) {
        if (score >= 90) {
            return EXCELLENT;
        } else if (score >= 80) {
            return GOOD;
        } else if (score >= 70) {
            return FAIR;
        } else {
            return POOR;
        }
    }
    
    /**
     * 检查得分是否在范围内
     */
    public boolean isScoreInRange(double score) {
        return score >= minScore && score <= maxScore;
    }
    
    /**
     * 获取等级颜色（用于前端显示）
     */
    public String getColor() {
        switch (this) {
            case EXCELLENT:
                return "success";
            case GOOD:
                return "primary";
            case FAIR:
                return "warning";
            case POOR:
                return "danger";
            default:
                return "default";
        }
    }
    
    /**
     * 获取等级图标（用于前端显示）
     */
    public String getIcon() {
        switch (this) {
            case EXCELLENT:
                return "star";
            case GOOD:
                return "check-circle";
            case FAIR:
                return "exclamation-circle";
            case POOR:
                return "times-circle";
            default:
                return "question-circle";
        }
    }
}
