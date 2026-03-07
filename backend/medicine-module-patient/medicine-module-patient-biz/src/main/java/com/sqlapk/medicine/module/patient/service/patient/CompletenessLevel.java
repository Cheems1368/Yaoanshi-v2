package com.sqlapk.medicine.module.patient.service.patient;

/**
 * 患者档案完整性等级枚举
 */
public enum CompletenessLevel {
    
    /**
     * 红层 - 必填字段缺失
     */
    RED(1, "红层"),
    
    /**
     * 黄层 - 重要字段缺失
     */
    YELLOW(2, "黄层"),
    
    /**
     * 蓝层 - 可选字段缺失
     */
    BLUE(3, "蓝层");
    
    private final Integer level;
    private final String description;
    
    CompletenessLevel(Integer level, String description) {
        this.level = level;
        this.description = description;
    }
    
    public Integer getLevel() {
        return level;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据评分获取等级
     */
    public static CompletenessLevel fromScore(double score) {
        if (score >= 90) {
            return BLUE;
        } else if (score >= 70) {
            return YELLOW;
        } else {
            return RED;
        }
    }
}
