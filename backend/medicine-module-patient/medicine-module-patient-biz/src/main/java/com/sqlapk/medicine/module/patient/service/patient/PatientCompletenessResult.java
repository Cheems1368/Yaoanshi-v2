package com.sqlapk.medicine.module.patient.service.patient;

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
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 检查时间
     */
    private LocalDateTime checkTime;
    
    /**
     * 必填字段是否完整
     */
    private boolean requiredFieldsComplete;
    
    /**
     * 重要字段是否完整
     */
    private boolean importantFieldsComplete;
    
    /**
     * 完整性评分 (0-100)
     */
    private double completenessScore;
    
    /**
     * 完整性等级 (1-红层, 2-黄层, 3-蓝层)
     */
    private Integer completenessLevel;
    
    /**
     * 缺失的必填字段
     */
    private List<String> missingRequiredFields = new ArrayList<>();
    
    /**
     * 缺失的重要字段
     */
    private List<String> missingImportantFields = new ArrayList<>();
    
    /**
     * 缺失的可选字段
     */
    private List<String> missingOptionalFields = new ArrayList<>();
    
    /**
     * 建议
     */
    private List<String> suggestions = new ArrayList<>();
    
    /**
     * 检查是否通过
     */
    private boolean passed;
    
    /**
     * 检查消息
     */
    private String message;
    
    /**
     * 添加缺失的必填字段
     */
    public void addMissingRequiredField(String field) {
        this.missingRequiredFields.add(field);
    }
    
    /**
     * 添加缺失的重要字段
     */
    public void addMissingImportantField(String field) {
        this.missingImportantFields.add(field);
    }
    
    /**
     * 添加缺失的可选字段
     */
    public void addMissingOptionalField(String field) {
        this.missingOptionalFields.add(field);
    }
    
    /**
     * 计算完整性评分
     */
    public void calculateScore() {
        int totalFields = 20; // 总字段数
        int missingCount = missingRequiredFields.size() + missingImportantFields.size() + missingOptionalFields.size();
        
        this.completenessScore = Math.max(0, 100 - (missingCount * 5));
        
        // 根据评分确定等级
        if (completenessScore >= 90) {
            this.completenessLevel = 3; // 蓝层
        } else if (completenessScore >= 70) {
            this.completenessLevel = 2; // 黄层
        } else {
            this.completenessLevel = 1; // 红层
        }
        
        // 检查是否通过（红层必填字段不能缺失）
        this.passed = missingRequiredFields.isEmpty();
        
        // 生成消息
        if (passed) {
            this.message = "档案完整性检查通过";
        } else {
            this.message = "档案完整性检查失败，缺失必填字段: " + String.join(", ", missingRequiredFields);
        }
    }
}
