package com.sqlapk.medicine.module.patient.service.patient;

import com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientAuditRecordDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.patient.PatientAuditRecordMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.patient.PatientsMapper;
import com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants;
import com.sqlapk.medicine.module.patient.enums.PatientAuditStatus;
import com.sqlapk.medicine.module.patient.service.patient.model.PatientCompletenessResult;
import com.sqlapk.medicine.module.patient.service.patient.model.CompletenessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 患者档案审核服务
 */
@Slf4j
@Service
public class PatientAuditService {

    @Autowired
    private PatientProfileCompletenessChecker completenessChecker;

    @Autowired
    private PatientsService patientsService;

    @Autowired
    private PatientAuditRecordMapper patientAuditRecordMapper;

    @Autowired
    private PatientsMapper patientsMapper;

    /**
     * 提交患者档案审核
     */
    @Transactional
    public void submitForAudit(Long patientId, Long auditorId) {
        PatientsDO patient = patientsService.gets(patientId);
        if (patient == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PATIENT_NOT_EXISTS);
        }

        // 检查档案完整性
        PatientCompletenessResult completeness = completenessChecker.checkCompleteness(patient);

        // 创建审核记录
        PatientAuditRecordDO auditRecord = PatientAuditRecordDO.builder()
                .patientId(patientId)
                .auditorId(auditorId)
                .auditStatus(PatientAuditStatus.PENDING.getCode())
                .completenessScore(completeness.getCompletenessScore())
                .completenessLevel(completeness.getCompletenessLevel().getDescription())
                .submitTime(LocalDateTime.now())
                .build();

        // 保存审核记录
        patientAuditRecordMapper.insert(auditRecord);

        // 更新患者审核状态
        patient.setAuditStatus(PatientAuditStatus.PENDING.getCode());
        patient.setCompletenessScore(completeness.getCompletenessScore());
        patient.setCompletenessLevel(completeness.getCompletenessLevel().getDescription());
        patientsMapper.updateById(patient);

        log.info("患者档案提交审核成功，患者ID: {}, 审核人ID: {}", patientId, auditorId);
    }

    /**
     * 审核患者档案
     */
    @Transactional
    public void auditPatient(Long patientId, Long auditorId, 
                           PatientAuditStatus status, String comment) {
        PatientsDO patient = patientsService.gets(patientId);
        if (patient == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PATIENT_NOT_EXISTS);
        }

        // 更新审核记录
        PatientAuditRecordDO auditRecord = getLatestAuditRecord(patientId);
        if (auditRecord == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PATIENT_AUDIT_RECORD_NOT_EXISTS);
        }

        auditRecord.setAuditStatus(status.getCode());
        auditRecord.setAuditorId(auditorId);
        auditRecord.setAuditComment(comment);
        auditRecord.setAuditTime(LocalDateTime.now());
        patientAuditRecordMapper.updateById(auditRecord);

        // 更新患者审核状态
        patient.setAuditStatus(status.getCode());
        patientsMapper.updateById(patient);

        log.info("患者档案审核完成，患者ID: {}, 审核人ID: {}, 状态: {}", 
                patientId, auditorId, status.getDescription());
    }

    /**
     * 获取最新的审核记录
     */
    public PatientAuditRecordDO getLatestAuditRecord(Long patientId) {
        return patientAuditRecordMapper.selectLatestByPatientId(patientId);
    }

    /**
     * 获取患者审核历史
     */
    public List<PatientAuditRecordDO> getAuditHistory(Long patientId) {
        return patientAuditRecordMapper.selectListByPatientId(patientId);
    }

    /**
     * 检查患者是否可以提交审核
     */
    public boolean canSubmitForAudit(Long patientId) {
        PatientsDO patient = patientsService.gets(patientId);
        if (patient == null) {
            return false;
        }

        // 检查当前审核状态
        PatientAuditStatus currentStatus = PatientAuditStatus.fromCode(patient.getAuditStatus());
        if (currentStatus == null || !currentStatus.canModify()) {
            return false;
        }

        // 检查档案完整性
        PatientCompletenessResult completeness = completenessChecker.checkCompleteness(patient);
        return completeness.isAuditPassable();
    }

    /**
     * 批量审核患者档案
     */
    @Transactional
    public void batchAuditPatients(List<Long> patientIds, Long auditorId, 
                                 PatientAuditStatus status, String comment) {
        for (Long patientId : patientIds) {
            try {
                auditPatient(patientId, auditorId, status, comment);
            } catch (Exception e) {
                log.error("批量审核患者档案失败，患者ID: {}, 错误: {}", patientId, e.getMessage());
            }
        }
    }

}
