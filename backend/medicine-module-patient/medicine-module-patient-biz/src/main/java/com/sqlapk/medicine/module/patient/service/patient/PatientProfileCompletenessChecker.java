package com.sqlapk.medicine.module.patient.service.patient;

import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;
import com.sqlapk.medicine.module.patient.service.patient.model.PatientCompletenessResult;
import com.sqlapk.medicine.module.patient.service.patient.model.CompletenessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 患者档案完整性检查器
 * 用于检查患者档案的完整性和必填字段
 */
@Slf4j
@Component
public class PatientProfileCompletenessChecker {

    /**
     * 检查患者档案完整性
     */
    public PatientCompletenessResult checkCompleteness(PatientsDO patient) {
        PatientCompletenessResult result = new PatientCompletenessResult();
        
        // 检查红层必填字段
        checkRequiredFields(patient, result);
        
        // 检查橙层重要字段
        checkImportantFields(patient, result);
        
        // 计算完整性得分
        calculateCompletenessScore(result);
        
        // 设置检查时间
        result.setCheckTime(java.time.LocalDateTime.now());
        
        log.debug("患者档案完整性检查完成，患者ID: {}, 得分: {}", 
                 patient.getId(), result.getCompletenessScore());
        
        return result;
    }
    
    /**
     * 检查红层必填字段
     */
    private void checkRequiredFields(PatientsDO patient, PatientCompletenessResult result) {
        List<String> missingFields = new ArrayList<>();
        
        // 基础信息检查
        if (!StringUtils.hasText(patient.getName())) {
            missingFields.add("患者姓名");
        }
        if (patient.getGender() == null) {
            missingFields.add("患者性别");
        }
        if (patient.getBirthday() == null) {
            missingFields.add("患者生日");
        }
        if (!StringUtils.hasText(patient.getMobile())) {
            missingFields.add("患者手机号");
        }
        if (!StringUtils.hasText(patient.getIdCard())) {
            missingFields.add("患者身份证号");
        }
        
        // 医疗信息检查
        if (patient.getPatientType() == null) {
            missingFields.add("患者类型");
        }
        if (!StringUtils.hasText(patient.getAddress())) {
            missingFields.add("家庭住址");
        }
        if (!StringUtils.hasText(patient.getAllergyHistory())) {
            missingFields.add("过敏史");
        }
        if (!StringUtils.hasText(patient.getMedicalHistory())) {
            missingFields.add("患病史");
        }
        
        result.setMissingRequiredFields(missingFields);
        result.setRequiredFieldsComplete(missingFields.isEmpty());
    }
    
    /**
     * 检查橙层重要字段
     */
    private void checkImportantFields(PatientsDO patient, PatientCompletenessResult result) {
        List<String> missingFields = new ArrayList<>();
        
        if (!StringUtils.hasText(patient.getFamilyHistory())) {
            missingFields.add("家族史");
        }
        if (!StringUtils.hasText(patient.getPersonalHistory())) {
            missingFields.add("个人史");
        }
        if (!StringUtils.hasText(patient.getInsuranceCardNumber())) {
            missingFields.add("医保卡号");
        }
        if (patient.getInsurance() == null) {
            missingFields.add("报销类型");
        }
        if (!StringUtils.hasText(patient.getEmergencyContact())) {
            missingFields.add("紧急联系人");
        }
        if (!StringUtils.hasText(patient.getEmergencyPhone())) {
            missingFields.add("紧急联系电话");
        }
        
        result.setMissingImportantFields(missingFields);
        result.setImportantFieldsComplete(missingFields.isEmpty());
    }
    
    /**
     * 计算完整性得分
     */
    private void calculateCompletenessScore(PatientCompletenessResult result) {
        int totalFields = 15; // 总字段数
        int missingFields = result.getMissingRequiredFields().size() + 
                           result.getMissingImportantFields().size();
        
        double score = (double) (totalFields - missingFields) / totalFields * 100;
        result.setCompletenessScore(score);
        
        // 设置完整性等级
        result.setCompletenessLevel(CompletenessLevel.fromScore(score));
    }
    
    /**
     * 根据患者类型检查特定字段
     */
    public PatientCompletenessResult checkCompletenessByPatientType(PatientsDO patient) {
        PatientCompletenessResult result = checkCompleteness(patient);
        
        // 根据患者类型调整必填字段
        if (patient.getPatientType() != null) {
            switch (patient.getPatientType()) {
                case 0: // 住院患者
                    checkInpatientRequiredFields(patient, result);
                    break;
                case 1: // 门诊患者
                    checkOutpatientRequiredFields(patient, result);
                    break;
                case 2: // 居家患者
                    checkHomeCareRequiredFields(patient, result);
                    break;
            }
        }
        
        // 重新计算得分
        calculateCompletenessScore(result);
        
        return result;
    }
    
    /**
     * 检查住院患者必填字段
     */
    private void checkInpatientRequiredFields(PatientsDO patient, PatientCompletenessResult result) {
        // 住院患者需要所有字段
        // 已经在基础检查中处理
    }
    
    /**
     * 检查门诊患者必填字段
     */
    private void checkOutpatientRequiredFields(PatientsDO patient, PatientCompletenessResult result) {
        // 门诊患者紧急联系人不是必填
        List<String> missingFields = result.getMissingImportantFields();
        missingFields.remove("紧急联系人");
        missingFields.remove("紧急联系电话");
        result.setMissingImportantFields(missingFields);
    }
    
    /**
     * 检查居家患者必填字段
     */
    private void checkHomeCareRequiredFields(PatientsDO patient, PatientCompletenessResult result) {
        // 居家患者紧急联系人是必填
        List<String> missingFields = result.getMissingRequiredFields();
        if (!StringUtils.hasText(patient.getEmergencyContact())) {
            missingFields.add("紧急联系人");
        }
        if (!StringUtils.hasText(patient.getEmergencyPhone())) {
            missingFields.add("紧急联系电话");
        }
        result.setMissingRequiredFields(missingFields);
        
        // 从重要字段中移除
        List<String> importantFields = result.getMissingImportantFields();
        importantFields.remove("紧急联系人");
        importantFields.remove("紧急联系电话");
        result.setMissingImportantFields(importantFields);
    }
}
