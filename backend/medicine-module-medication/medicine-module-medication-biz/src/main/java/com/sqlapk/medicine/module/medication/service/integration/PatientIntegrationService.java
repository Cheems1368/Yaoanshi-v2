package com.sqlapk.medicine.module.medication.service.integration;

import com.sqlapk.medicine.module.patient.api.PatientsApi;
import com.sqlapk.medicine.module.patient.api.dto.PatientsRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 患者档案集成服务
 */
@Slf4j
@Service
public class PatientIntegrationService {

    @Autowired
    private PatientsApi patientsApi;

    /**
     * 获取患者档案信息
     */
    public PatientsRespDTO getPatientProfile(Long patientId) {
        try {
            log.debug("获取患者档案信息: {}", patientId);
            return patientsApi.getPatients(patientId);
        } catch (Exception e) {
            log.error("获取患者档案信息失败: {}, 错误: {}", patientId, e.getMessage());
            return null;
        }
    }

    /**
     * 批量获取患者档案信息
     */
    public List<PatientsRespDTO> getPatientProfiles(List<Long> patientIds) {
        try {
            log.debug("批量获取患者档案信息: {}", patientIds);
            return patientsApi.getPatientsList(patientIds);
        } catch (Exception e) {
            log.error("批量获取患者档案信息失败: {}, 错误: {}", patientIds, e.getMessage());
            return null;
        }
    }

    /**
     * 验证患者是否存在
     */
    public boolean validatePatientExists(Long patientId) {
        try {
            PatientsRespDTO patient = patientsApi.getPatients(patientId);
            return patient != null;
        } catch (Exception e) {
            log.error("验证患者存在性失败: {}, 错误: {}", patientId, e.getMessage());
            return false;
        }
    }

    /**
     * 获取患者完整度信息
     */
    public PatientCompletenessInfo getPatientCompleteness(Long patientId) {
        try {
            PatientsRespDTO patient = patientsApi.getPatients(patientId);
            if (patient == null) {
                return null;
            }

            PatientCompletenessInfo completeness = new PatientCompletenessInfo();
            completeness.setPatientId(patientId);
            completeness.setCompletenessScore(patient.getCompletenessScore());
            completeness.setCompletenessLevel(patient.getCompletenessLevel());
            completeness.setAuditStatus(patient.getAuditStatus());
            completeness.setAuditPassable(isAuditPassable(patient));

            return completeness;
        } catch (Exception e) {
            log.error("获取患者完整度信息失败: {}, 错误: {}", patientId, e.getMessage());
            return null;
        }
    }

    /**
     * 检查患者是否可以通过审核
     */
    private boolean isAuditPassable(PatientsRespDTO patient) {
        // 检查必填字段
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            return false;
        }
        if (patient.getGender() == null) {
            return false;
        }
        if (patient.getBirthday() == null) {
            return false;
        }
        if (patient.getMobile() == null || patient.getMobile().trim().isEmpty()) {
            return false;
        }
        if (patient.getIdCard() == null || patient.getIdCard().trim().isEmpty()) {
            return false;
        }
        if (patient.getPatientType() == null) {
            return false;
        }
        if (patient.getAddress() == null || patient.getAddress().trim().isEmpty()) {
            return false;
        }
        if (patient.getAllergyHistory() == null || patient.getAllergyHistory().trim().isEmpty()) {
            return false;
        }
        if (patient.getMedicalHistory() == null || patient.getMedicalHistory().trim().isEmpty()) {
            return false;
        }

        // 检查完整度得分
        if (patient.getCompletenessScore() != null && patient.getCompletenessScore() < 70) {
            return false;
        }

        return true;
    }

    /**
     * 患者完整度信息
     */
    public static class PatientCompletenessInfo {
        private Long patientId;
        private Double completenessScore;
        private String completenessLevel;
        private Integer auditStatus;
        private boolean auditPassable;

        // Getters and Setters
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }

        public Double getCompletenessScore() { return completenessScore; }
        public void setCompletenessScore(Double completenessScore) { this.completenessScore = completenessScore; }

        public String getCompletenessLevel() { return completenessLevel; }
        public void setCompletenessLevel(String completenessLevel) { this.completenessLevel = completenessLevel; }

        public Integer getAuditStatus() { return auditStatus; }
        public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }

        public boolean isAuditPassable() { return auditPassable; }
        public void setAuditPassable(boolean auditPassable) { this.auditPassable = auditPassable; }
    }
}
