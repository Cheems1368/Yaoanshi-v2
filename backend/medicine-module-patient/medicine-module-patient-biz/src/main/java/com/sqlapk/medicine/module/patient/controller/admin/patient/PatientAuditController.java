package com.sqlapk.medicine.module.patient.controller.admin.patient;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientAuditPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientAuditReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientCompletenessRespVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientAuditRecordDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;
import com.sqlapk.medicine.module.patient.enums.PatientAuditStatus;
import com.sqlapk.medicine.module.patient.service.patient.PatientAuditService;
import com.sqlapk.medicine.module.patient.service.patient.PatientProfileCompletenessChecker;
import com.sqlapk.medicine.module.patient.service.patient.PatientsService;
import com.sqlapk.medicine.module.patient.service.patient.model.PatientCompletenessResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 患者档案审核
 */
@Tag(name = "管理后台 - 患者档案审核")
@RestController
@RequestMapping("/patient/audit")
@Validated
@Slf4j
public class PatientAuditController {

    @Resource
    private PatientAuditService patientAuditService;

    @Resource
    private PatientProfileCompletenessChecker completenessChecker;

    @Resource
    private PatientsService patientsService;

    @PostMapping("/submit")
    @Operation(summary = "提交患者档案审核")
    @PreAuthorize("@ss.hasPermission('patient:audit:submit')")
    public CommonResult<Boolean> submitForAudit(@Valid @RequestBody PatientAuditReqVO.SubmitReqVO reqVO) {
        patientAuditService.submitForAudit(reqVO.getPatientId(), reqVO.getAuditorId());
        return success(true);
    }

    @PostMapping("/audit")
    @Operation(summary = "审核患者档案")
    @PreAuthorize("@ss.hasPermission('patient:audit:audit')")
    public CommonResult<Boolean> auditPatient(@Valid @RequestBody PatientAuditReqVO.AuditReqVO reqVO) {
        PatientAuditStatus status = PatientAuditStatus.fromCode(reqVO.getAuditStatus());
        patientAuditService.auditPatient(reqVO.getPatientId(), reqVO.getAuditorId(), 
                                        status, reqVO.getAuditComment());
        return success(true);
    }

    @GetMapping("/completeness/{patientId}")
    @Operation(summary = "获取患者档案完整性检查结果")
    @PreAuthorize("@ss.hasPermission('patient:audit:view')")
    public CommonResult<PatientCompletenessRespVO> getCompleteness(
            @Parameter(description = "患者ID", required = true) @PathVariable("patientId") Long patientId) {
        PatientsDO patient = patientsService.gets(patientId);
        if (patient == null) {
            return success(null);
        }

        PatientCompletenessResult result = completenessChecker.checkCompleteness(patient);
        PatientCompletenessRespVO respVO = PatientCompletenessRespVO.builder()
                .patientId(patientId)
                .completenessScore(result.getCompletenessScore())
                .completenessLevel(result.getCompletenessLevel().getDescription())
                .completenessDescription(result.getCompletenessDescription())
                .requiredFieldsComplete(result.isRequiredFieldsComplete())
                .importantFieldsComplete(result.isImportantFieldsComplete())
                .missingRequiredFields(result.getMissingRequiredFields())
                .missingImportantFields(result.getMissingImportantFields())
                .totalMissingFields(result.getTotalMissingFields())
                .auditPassable(result.isAuditPassable())
                .checkTime(result.getCheckTime())
                .build();

        return success(respVO);
    }

    @GetMapping("/history/{patientId}")
    @Operation(summary = "获取患者审核历史")
    @PreAuthorize("@ss.hasPermission('patient:audit:view')")
    public CommonResult<List<PatientAuditRecordDO>> getAuditHistory(
            @Parameter(description = "患者ID", required = true) @PathVariable("patientId") Long patientId) {
        List<PatientAuditRecordDO> history = patientAuditService.getAuditHistory(patientId);
        return success(history);
    }

    @GetMapping("/latest/{patientId}")
    @Operation(summary = "获取患者最新审核记录")
    @PreAuthorize("@ss.hasPermission('patient:audit:view')")
    public CommonResult<PatientAuditRecordDO> getLatestAuditRecord(
            @Parameter(description = "患者ID", required = true) @PathVariable("patientId") Long patientId) {
        PatientAuditRecordDO record = patientAuditService.getLatestAuditRecord(patientId);
        return success(record);
    }

    @GetMapping("/can-submit/{patientId}")
    @Operation(summary = "检查患者是否可以提交审核")
    @PreAuthorize("@ss.hasPermission('patient:audit:view')")
    public CommonResult<Boolean> canSubmitForAudit(
            @Parameter(description = "患者ID", required = true) @PathVariable("patientId") Long patientId) {
        boolean canSubmit = patientAuditService.canSubmitForAudit(patientId);
        return success(canSubmit);
    }

    @PostMapping("/batch-audit")
    @Operation(summary = "批量审核患者档案")
    @PreAuthorize("@ss.hasPermission('patient:audit:audit')")
    public CommonResult<Boolean> batchAuditPatients(@Valid @RequestBody PatientAuditReqVO.BatchAuditReqVO reqVO) {
        PatientAuditStatus status = PatientAuditStatus.fromCode(reqVO.getAuditStatus());
        patientAuditService.batchAuditPatients(reqVO.getPatientIds(), reqVO.getAuditorId(), 
                                             status, reqVO.getAuditComment());
        return success(true);
    }

}
