package com.sqlapk.medicine.module.medication.controller.admin.integration;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.module.medication.controller.admin.integration.vo.IntegrationStatsRespVO;
import com.sqlapk.medicine.module.medication.controller.admin.integration.vo.MedicationPlanValidationReqVO;
import com.sqlapk.medicine.module.medication.controller.admin.integration.vo.MedicationPlanValidationRespVO;
import com.sqlapk.medicine.module.medication.controller.admin.integration.vo.ReportGenerationValidationReqVO;
import com.sqlapk.medicine.module.medication.controller.admin.integration.vo.ReportGenerationValidationRespVO;
import com.sqlapk.medicine.module.medication.service.integration.IntegrationManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 集成服务控制器
 */
@Tag(name = "管理后台 - 集成服务")
@RestController
@RequestMapping("/medication/integration")
@Slf4j
public class IntegrationController {

    @Autowired
    private IntegrationManager integrationManager;

    @PostMapping("/validate-medication-plan")
    @Operation(summary = "验证用药计划")
    public CommonResult<MedicationPlanValidationRespVO> validateMedicationPlan(@RequestBody MedicationPlanValidationReqVO reqVO) {
        try {
            IntegrationManager.MedicationPlanValidationResult result = 
                    integrationManager.validateMedicationPlan(reqVO.getPatientId(), reqVO.getDrugId(), reqVO.getUserId());
            
            MedicationPlanValidationRespVO respVO = new MedicationPlanValidationRespVO();
            respVO.setValid(result.isValid());
            respVO.setPatientValid(result.isPatientValid());
            respVO.setDrugValid(result.isDrugValid());
            respVO.setUserValid(result.isUserValid());
            respVO.setCanModify(result.isCanModify());
            respVO.setDrugAvailable(result.isDrugAvailable());
            respVO.setDrugControlled(result.isDrugControlled());
            respVO.setPatientAuditPassable(result.isPatientAuditPassable());
            respVO.setPatientCompletenessScore(result.getPatientCompletenessScore());
            respVO.setAllergyRiskLevel(result.getAllergyRiskLevel());
            respVO.setAllergyRiskDescription(result.getAllergyRiskDescription());
            respVO.setErrors(result.getErrors());
            respVO.setWarnings(result.getWarnings());
            respVO.setValidationTime(result.getValidationTime());
            
            return CommonResult.success(respVO);
        } catch (Exception e) {
            log.error("验证用药计划失败: {}", e.getMessage(), e);
            return CommonResult.error(500, "验证用药计划失败: " + e.getMessage());
        }
    }

    @PostMapping("/validate-report-generation")
    @Operation(summary = "验证报告生成权限")
    public CommonResult<ReportGenerationValidationRespVO> validateReportGeneration(@RequestBody ReportGenerationValidationReqVO reqVO) {
        try {
            IntegrationManager.ReportGenerationValidationResult result = 
                    integrationManager.validateReportGeneration(reqVO.getPatientId(), reqVO.getUserId(), reqVO.getReportType());
            
            ReportGenerationValidationRespVO respVO = new ReportGenerationValidationRespVO();
            respVO.setValid(result.isValid());
            respVO.setPatientValid(result.isPatientValid());
            respVO.setUserValid(result.isUserValid());
            respVO.setCanGenerate(result.isCanGenerate());
            respVO.setPatientAuditPassable(result.isPatientAuditPassable());
            respVO.setPatientCompletenessScore(result.getPatientCompletenessScore());
            respVO.setErrors(result.getErrors());
            respVO.setWarnings(result.getWarnings());
            respVO.setValidationTime(result.getValidationTime());
            
            return CommonResult.success(respVO);
        } catch (Exception e) {
            log.error("验证报告生成权限失败: {}", e.getMessage(), e);
            return CommonResult.error(500, "验证报告生成权限失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "获取集成服务统计")
    public CommonResult<Map<String, IntegrationStatsRespVO>> getIntegrationStats() {
        try {
            Map<String, IntegrationManager.IntegrationStats> statsMap = 
                    integrationManager.getAllIntegrationStats();
            
            Map<String, IntegrationStatsRespVO> respMap = new HashMap<>();
            for (Map.Entry<String, IntegrationManager.IntegrationStats> entry : statsMap.entrySet()) {
                IntegrationManager.IntegrationStats stats = entry.getValue();
                IntegrationStatsRespVO respVO = new IntegrationStatsRespVO();
                respVO.setServiceName(entry.getKey());
                respVO.setTotalCalls(stats.getTotalCalls());
                respVO.setSuccessCalls(stats.getSuccessCalls());
                respVO.setFailureCalls(stats.getFailureCalls());
                respVO.setSuccessRate(stats.getSuccessRate());
                respVO.setLastCallTime(stats.getLastCallTime());
                respMap.put(entry.getKey(), respVO);
            }
            
            return CommonResult.success(respMap);
        } catch (Exception e) {
            log.error("获取集成服务统计失败: {}", e.getMessage(), e);
            return CommonResult.error(500, "获取集成服务统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats/{serviceName}")
    @Operation(summary = "获取指定集成服务统计")
    public CommonResult<IntegrationStatsRespVO> getIntegrationStats(@PathVariable String serviceName) {
        try {
            IntegrationManager.IntegrationStats stats = 
                    integrationManager.getIntegrationStats(serviceName);
            
            if (stats == null) {
                return CommonResult.error(404, "集成服务不存在: " + serviceName);
            }
            
            IntegrationStatsRespVO respVO = new IntegrationStatsRespVO();
            respVO.setServiceName(serviceName);
            respVO.setTotalCalls(stats.getTotalCalls());
            respVO.setSuccessCalls(stats.getSuccessCalls());
            respVO.setFailureCalls(stats.getFailureCalls());
            respVO.setSuccessRate(stats.getSuccessRate());
            respVO.setLastCallTime(stats.getLastCallTime());
            
            return CommonResult.success(respVO);
        } catch (Exception e) {
            log.error("获取集成服务统计失败: {}", e.getMessage(), e);
            return CommonResult.error(500, "获取集成服务统计失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/stats")
    @Operation(summary = "清理集成服务统计")
    public CommonResult<Void> clearIntegrationStats() {
        try {
            integrationManager.clearIntegrationStats();
            return CommonResult.success(null);
        } catch (Exception e) {
            log.error("清理集成服务统计失败: {}", e.getMessage(), e);
            return CommonResult.error(500, "清理集成服务统计失败: " + e.getMessage());
        }
    }
}
