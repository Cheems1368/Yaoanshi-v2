package com.sqlapk.medicine.module.medication.service.integration;

import com.sqlapk.medicine.module.patient.api.dto.PatientsRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 集成服务管理器
 */
@Service
@Slf4j
public class IntegrationManager {

    @Autowired
    private PatientIntegrationService patientIntegrationService;

    @Autowired
    private DrugIntegrationService drugIntegrationService;

    @Autowired
    private UserPermissionIntegrationService userPermissionIntegrationService;

    // 集成服务统计
    private final ConcurrentHashMap<String, IntegrationStats> integrationStats = new ConcurrentHashMap<>();

    /**
     * 集成服务统计信息
     */
    public static class IntegrationStats {
        private final AtomicLong totalCalls = new AtomicLong(0);
        private final AtomicLong successCalls = new AtomicLong(0);
        private final AtomicLong failureCalls = new AtomicLong(0);
        private final AtomicLong lastCallTime = new AtomicLong(0);

        public long getTotalCalls() { return totalCalls.get(); }
        public long getSuccessCalls() { return successCalls.get(); }
        public long getFailureCalls() { return failureCalls.get(); }
        public long getLastCallTime() { return lastCallTime.get(); }
        public double getSuccessRate() {
            long total = getTotalCalls();
            return total > 0 ? (double) successCalls.get() / total : 0.0;
        }

        public void recordCall(boolean success) {
            totalCalls.incrementAndGet();
            if (success) {
                successCalls.incrementAndGet();
            } else {
                failureCalls.incrementAndGet();
            }
            lastCallTime.set(System.currentTimeMillis());
        }
    }

    /**
     * 验证用药计划完整性
     */
    public MedicationPlanValidationResult validateMedicationPlan(Long patientId, Long drugId, Long userId) {
        MedicationPlanValidationResult result = new MedicationPlanValidationResult();
        result.setPatientId(patientId);
        result.setDrugId(drugId);
        result.setUserId(userId);
        result.setValidationTime(LocalDateTime.now());

        try {
            // 验证患者信息
            boolean patientValid = patientIntegrationService.validatePatientExists(patientId);
            result.setPatientValid(patientValid);
            if (!patientValid) {
                result.addError("患者不存在");
            }

            // 验证药品信息
            boolean drugValid = drugIntegrationService.validateDrugExists(drugId);
            result.setDrugValid(drugValid);
            if (!drugValid) {
                result.addError("药品不存在");
            }

            // 验证用户权限
            boolean userValid = userPermissionIntegrationService.validateUserExists(userId);
            result.setUserValid(userValid);
            if (!userValid) {
                result.addError("用户不存在");
            }

            // 检查用户权限
            if (userValid) {
                boolean canModify = userPermissionIntegrationService.canModifyMedicationPlan(userId, patientId);
                result.setCanModify(canModify);
                if (!canModify) {
                    result.addError("用户无权限修改用药计划");
                }
            }

            // 检查患者档案完整性
            if (patientValid) {
                PatientIntegrationService.PatientCompletenessInfo completeness = 
                        patientIntegrationService.getPatientCompleteness(patientId);
                if (completeness != null) {
                    result.setPatientCompletenessScore(completeness.getCompletenessScore());
                    result.setPatientAuditPassable(completeness.isAuditPassable());
                    if (!completeness.isAuditPassable()) {
                        result.addWarning("患者档案未通过审核");
                    }
                }
            }

            // 检查药品可用性
            if (drugValid) {
                boolean drugAvailable = drugIntegrationService.isDrugAvailable(drugId);
                result.setDrugAvailable(drugAvailable);
                if (!drugAvailable) {
                    result.addError("药品不可用");
                }

                // 检查是否为管制药品
                boolean isControlled = drugIntegrationService.isControlledDrug(drugId);
                result.setDrugControlled(isControlled);
                if (isControlled) {
                    result.addWarning("该药品为管制药品，需要特殊管理");
                }
            }

            // 检查过敏风险
            if (patientValid && drugValid) {
                // 获取患者档案信息以获取过敏史
                PatientsRespDTO patientProfile = patientIntegrationService.getPatientProfile(patientId);
                String allergyHistory = "";
                if (patientProfile != null && patientProfile.getAllergyHistory() != null) {
                    allergyHistory = patientProfile.getAllergyHistory();
                }
                
                DrugIntegrationService.AllergyRiskInfo allergyRisk = 
                        drugIntegrationService.checkAllergyRisk(drugId, allergyHistory);
                if (allergyRisk != null) {
                    result.setAllergyRiskLevel(allergyRisk.getRiskLevel());
                    result.setAllergyRiskDescription(allergyRisk.getRiskDescription());
                    if ("HIGH".equals(allergyRisk.getRiskLevel())) {
                        result.addError("存在高过敏风险");
                    } else if ("MEDIUM".equals(allergyRisk.getRiskLevel())) {
                        result.addWarning("存在中等过敏风险");
                    }
                }
            }

            result.setValid(result.getErrors().isEmpty());
            
            // 记录统计
            recordIntegrationCall("medication-plan-validation", result.isValid());

        } catch (Exception e) {
            log.error("验证用药计划失败: 患者={}, 药品={}, 用户={}, 错误={}", 
                     patientId, drugId, userId, e.getMessage());
            result.addError("验证过程发生异常: " + e.getMessage());
            result.setValid(false);
            
            recordIntegrationCall("medication-plan-validation", false);
        }

        return result;
    }

    /**
     * 验证报告生成权限
     */
    public ReportGenerationValidationResult validateReportGeneration(Long patientId, Long userId, Integer reportType) {
        ReportGenerationValidationResult result = new ReportGenerationValidationResult();
        result.setPatientId(patientId);
        result.setUserId(userId);
        result.setReportType(reportType);
        result.setValidationTime(LocalDateTime.now());

        try {
            // 验证患者信息
            boolean patientValid = patientIntegrationService.validatePatientExists(patientId);
            result.setPatientValid(patientValid);
            if (!patientValid) {
                result.addError("患者不存在");
            }

            // 验证用户权限
            boolean userValid = userPermissionIntegrationService.validateUserExists(userId);
            result.setUserValid(userValid);
            if (!userValid) {
                result.addError("用户不存在");
            }

            // 检查用户权限
            if (userValid) {
                boolean canGenerate = userPermissionIntegrationService.canGenerateReport(userId, patientId);
                result.setCanGenerate(canGenerate);
                if (!canGenerate) {
                    result.addError("用户无权限生成报告");
                }
            }

            // 检查患者档案完整性
            if (patientValid) {
                PatientIntegrationService.PatientCompletenessInfo completeness = 
                        patientIntegrationService.getPatientCompleteness(patientId);
                if (completeness != null) {
                    result.setPatientCompletenessScore(completeness.getCompletenessScore());
                    result.setPatientAuditPassable(completeness.isAuditPassable());
                    if (!completeness.isAuditPassable()) {
                        result.addWarning("患者档案未通过审核，报告可能不完整");
                    }
                }
            }

            result.setValid(result.getErrors().isEmpty());
            
            // 记录统计
            recordIntegrationCall("report-generation-validation", result.isValid());

        } catch (Exception e) {
            log.error("验证报告生成权限失败: 患者={}, 用户={}, 报告类型={}, 错误={}", 
                     patientId, userId, reportType, e.getMessage());
            result.addError("验证过程发生异常: " + e.getMessage());
            result.setValid(false);
            
            recordIntegrationCall("report-generation-validation", false);
        }

        return result;
    }

    /**
     * 记录集成服务调用
     */
    private void recordIntegrationCall(String serviceName, boolean success) {
        IntegrationStats stats = integrationStats.computeIfAbsent(serviceName, k -> new IntegrationStats());
        stats.recordCall(success);
    }

    /**
     * 获取集成服务统计
     */
    public IntegrationStats getIntegrationStats(String serviceName) {
        return integrationStats.get(serviceName);
    }

    /**
     * 获取所有集成服务统计
     */
    public ConcurrentHashMap<String, IntegrationStats> getAllIntegrationStats() {
        return new ConcurrentHashMap<>(integrationStats);
    }

    /**
     * 清理集成服务统计
     */
    public void clearIntegrationStats() {
        integrationStats.clear();
        log.info("集成服务统计已清理");
    }

    /**
     * 用药计划验证结果
     */
    public static class MedicationPlanValidationResult {
        private Long patientId;
        private Long drugId;
        private Long userId;
        private LocalDateTime validationTime;
        private boolean valid = true;
        private boolean patientValid = false;
        private boolean drugValid = false;
        private boolean userValid = false;
        private boolean canModify = false;
        private boolean drugAvailable = false;
        private boolean drugControlled = false;
        private boolean patientAuditPassable = false;
        private Double patientCompletenessScore;
        private String allergyRiskLevel;
        private String allergyRiskDescription;
        private java.util.List<String> errors = new java.util.ArrayList<>();
        private java.util.List<String> warnings = new java.util.ArrayList<>();

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        // Getters and Setters
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }

        public Long getDrugId() { return drugId; }
        public void setDrugId(Long drugId) { this.drugId = drugId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public LocalDateTime getValidationTime() { return validationTime; }
        public void setValidationTime(LocalDateTime validationTime) { this.validationTime = validationTime; }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public boolean isPatientValid() { return patientValid; }
        public void setPatientValid(boolean patientValid) { this.patientValid = patientValid; }

        public boolean isDrugValid() { return drugValid; }
        public void setDrugValid(boolean drugValid) { this.drugValid = drugValid; }

        public boolean isUserValid() { return userValid; }
        public void setUserValid(boolean userValid) { this.userValid = userValid; }

        public boolean isCanModify() { return canModify; }
        public void setCanModify(boolean canModify) { this.canModify = canModify; }

        public boolean isDrugAvailable() { return drugAvailable; }
        public void setDrugAvailable(boolean drugAvailable) { this.drugAvailable = drugAvailable; }

        public boolean isDrugControlled() { return drugControlled; }
        public void setDrugControlled(boolean drugControlled) { this.drugControlled = drugControlled; }

        public boolean isPatientAuditPassable() { return patientAuditPassable; }
        public void setPatientAuditPassable(boolean patientAuditPassable) { this.patientAuditPassable = patientAuditPassable; }

        public Double getPatientCompletenessScore() { return patientCompletenessScore; }
        public void setPatientCompletenessScore(Double patientCompletenessScore) { this.patientCompletenessScore = patientCompletenessScore; }

        public String getAllergyRiskLevel() { return allergyRiskLevel; }
        public void setAllergyRiskLevel(String allergyRiskLevel) { this.allergyRiskLevel = allergyRiskLevel; }

        public String getAllergyRiskDescription() { return allergyRiskDescription; }
        public void setAllergyRiskDescription(String allergyRiskDescription) { this.allergyRiskDescription = allergyRiskDescription; }

        public java.util.List<String> getErrors() { 
            return errors != null ? new java.util.ArrayList<>(errors) : null; 
        }
        public void setErrors(java.util.List<String> errors) { 
            this.errors = errors != null ? new java.util.ArrayList<>(errors) : null; 
        }

        public java.util.List<String> getWarnings() { 
            return warnings != null ? new java.util.ArrayList<>(warnings) : null; 
        }
        public void setWarnings(java.util.List<String> warnings) { 
            this.warnings = warnings != null ? new java.util.ArrayList<>(warnings) : null; 
        }
    }

    /**
     * 报告生成验证结果
     */
    public static class ReportGenerationValidationResult {
        private Long patientId;
        private Long userId;
        private Integer reportType;
        private LocalDateTime validationTime;
        private boolean valid = true;
        private boolean patientValid = false;
        private boolean userValid = false;
        private boolean canGenerate = false;
        private boolean patientAuditPassable = false;
        private Double patientCompletenessScore;
        private java.util.List<String> errors = new java.util.ArrayList<>();
        private java.util.List<String> warnings = new java.util.ArrayList<>();

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        // Getters and Setters
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Integer getReportType() { return reportType; }
        public void setReportType(Integer reportType) { this.reportType = reportType; }

        public LocalDateTime getValidationTime() { return validationTime; }
        public void setValidationTime(LocalDateTime validationTime) { this.validationTime = validationTime; }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public boolean isPatientValid() { return patientValid; }
        public void setPatientValid(boolean patientValid) { this.patientValid = patientValid; }

        public boolean isUserValid() { return userValid; }
        public void setUserValid(boolean userValid) { this.userValid = userValid; }

        public boolean isCanGenerate() { return canGenerate; }
        public void setCanGenerate(boolean canGenerate) { this.canGenerate = canGenerate; }

        public boolean isPatientAuditPassable() { return patientAuditPassable; }
        public void setPatientAuditPassable(boolean patientAuditPassable) { this.patientAuditPassable = patientAuditPassable; }

        public Double getPatientCompletenessScore() { return patientCompletenessScore; }
        public void setPatientCompletenessScore(Double patientCompletenessScore) { this.patientCompletenessScore = patientCompletenessScore; }

        public java.util.List<String> getErrors() { 
            return errors != null ? new java.util.ArrayList<>(errors) : null; 
        }
        public void setErrors(java.util.List<String> errors) { 
            this.errors = errors != null ? new java.util.ArrayList<>(errors) : null; 
        }

        public java.util.List<String> getWarnings() { 
            return warnings != null ? new java.util.ArrayList<>(warnings) : null; 
        }
        public void setWarnings(java.util.List<String> warnings) { 
            this.warnings = warnings != null ? new java.util.ArrayList<>(warnings) : null; 
        }
    }
}
