package com.sqlapk.medicine.module.patient.service.patient.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 患者档案完整性检查结果
 */
@Data
public class PatientCompletenessResult {
    
    /**
     * 缺失的必填字段
     */
    private List<String> missingRequiredFields = new ArrayList<>();
    
    /**
     * 缺失的重要字段
     */
    private List<String> missingImportantFields = new ArrayList<>();
    
    /**
     * 必填字段是否完整
     */
    private boolean requiredFieldsComplete;
    
    /**
     * 重要字段是否完整
     */
    private boolean importantFieldsComplete;
    
    /**
     * 完整性得分 (0-100)
     */
    private double completenessScore;
    
    /**
     * 完整性等级
     */
    private CompletenessLevel completenessLevel;
    
    /**
     * 检查时间
     */
    private LocalDateTime checkTime;
    
    /**
     * 是否可以通过审核
     */
    public boolean isAuditPassable() {
        return requiredFieldsComplete && completenessScore >= 70;
    }
    
    /**
     * 获取完整性描述
     */
    public String getCompletenessDescription() {
        if (completenessScore >= 90) {
            return "档案完整，信息详细";
        } else if (completenessScore >= 80) {
            return "档案基本完整，信息较详细";
        } else if (completenessScore >= 70) {
            return "档案基本完整，信息一般";
        } else {
            return "档案不完整，需要补充信息";
        }
    }
    
    /**
     * 获取缺失字段总数
     */
    public int getTotalMissingFields() {
        return missingRequiredFields.size() + missingImportantFields.size();
    }
    
    /**
     * 获取必填字段缺失数量
     */
    public int getMissingRequiredFieldsCount() {
        return missingRequiredFields.size();
    }
    
    /**
     * 获取重要字段缺失数量
     */
    public int getMissingImportantFieldsCount() {
        return missingImportantFields.size();
    }
}
