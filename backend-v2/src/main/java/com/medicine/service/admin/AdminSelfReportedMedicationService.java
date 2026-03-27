package com.medicine.service.admin;

import com.medicine.api.dto.medication.SelfReportedMedicationDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.helporder.HelpOrderEvent;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.domain.medication.SelfReportedMedication;
import com.medicine.repository.helporder.HelpOrderEventMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.medication.SelfReportedMedicationMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import com.medicine.service.notification.NotificationDispatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
public class AdminSelfReportedMedicationService {

    private static final long DEFAULT_TENANT_ID = 1L;
    private static final String OPERATOR_TAG = "admin-self-report";

    private final SelfReportedMedicationMapper selfReportedMedicationMapper;
    private final HelpOrderMapper helpOrderMapper;
    private final HelpOrderEventMapper helpOrderEventMapper;
    private final ProfessionalIdentityService professionalIdentityService;
    private final NotificationDispatchService notificationDispatchService;

    public AdminSelfReportedMedicationService(
            SelfReportedMedicationMapper selfReportedMedicationMapper,
            HelpOrderMapper helpOrderMapper,
            HelpOrderEventMapper helpOrderEventMapper,
            ProfessionalIdentityService professionalIdentityService,
            NotificationDispatchService notificationDispatchService
    ) {
        this.selfReportedMedicationMapper = selfReportedMedicationMapper;
        this.helpOrderMapper = helpOrderMapper;
        this.helpOrderEventMapper = helpOrderEventMapper;
        this.professionalIdentityService = professionalIdentityService;
        this.notificationDispatchService = notificationDispatchService;
    }

    public List<SelfReportedMedicationDTO> listForWorkOrder(JwtClaims claims, Long workOrderId) {
        Long tenantId = resolveTenantId(claims);
        return selfReportedMedicationMapper
                .selectByWorkOrderId(tenantId, workOrderId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public SelfReportedMedicationDTO acknowledge(JwtClaims claims, Long id) {
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        Long tenantId = resolveTenantId(claims);
        SelfReportedMedication srm = requireById(tenantId, id);
        if (!"PENDING".equals(srm.getStatus())) {
            throw new BusinessException(400, "SELF_REPORT_NOT_PENDING");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        srm.setStatus("ACTIVE");
        srm.setUpdater(claims.getSubject());
        srm.setUpdateTime(now);
        selfReportedMedicationMapper.update(srm);
        addWorkOrderEvent(tenantId, srm.getWorkOrderId(), "PHARMACIST_REPLY",
                "PHARMACIST", pharmacist.getId(), "已知晓并确认自报药物：" + srm.getDrugName(), now);
        notificationDispatchService.createAndDispatch(
                tenantId, OPERATOR_TAG,
                "PATIENT", srm.getPatientId(),
                "SELF_MEDICATION_ACKNOWLEDGED", srm.getId(),
                Map.of("drugName", srm.getDrugName() != null ? srm.getDrugName() : ""), now);
        return toDto(srm);
    }

    @Transactional
    public SelfReportedMedicationDTO reject(JwtClaims claims, Long id, String reason) {
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        Long tenantId = resolveTenantId(claims);
        SelfReportedMedication srm = requireById(tenantId, id);
        if (!"PENDING".equals(srm.getStatus())) {
            throw new BusinessException(400, "SELF_REPORT_NOT_PENDING");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        srm.setStatus("STOPPED");
        srm.setUpdater(claims.getSubject());
        srm.setUpdateTime(now);
        selfReportedMedicationMapper.update(srm);
        String eventContent = "驳回自报药物：" + srm.getDrugName() +
                (StringUtils.hasText(reason) ? "，原因：" + reason.trim() : "");
        addWorkOrderEvent(tenantId, srm.getWorkOrderId(), "PHARMACIST_REPLY",
                "PHARMACIST", pharmacist.getId(), eventContent, now);
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("drugName", srm.getDrugName() != null ? srm.getDrugName() : "");
        if (StringUtils.hasText(reason)) {
            payload.put("reason", reason.trim());
        }
        notificationDispatchService.createAndDispatch(
                tenantId, OPERATOR_TAG,
                "PATIENT", srm.getPatientId(),
                "SELF_MEDICATION_REJECTED", srm.getId(),
                payload, now);
        return toDto(srm);
    }

    public Map<String, Object> getTransferPrefill(JwtClaims claims, Long id) {
        Long tenantId = resolveTenantId(claims);
        SelfReportedMedication srm = requireById(tenantId, id);
        return Map.of(
                "drugName", srm.getDrugName() != null ? srm.getDrugName() : "",
                "dosage", srm.getDosage() != null ? srm.getDosage() : "",
                "patientId", srm.getPatientId(),
                "selfReportedMedicationId", srm.getId()
        );
    }

    @Transactional
    public void markTransferred(JwtClaims claims, Long id, Long planItemId) {
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        Long tenantId = resolveTenantId(claims);
        SelfReportedMedication srm = requireById(tenantId, id);
        if (!"ACTIVE".equals(srm.getStatus()) && !"PENDING".equals(srm.getStatus())) {
            throw new BusinessException(400, "SELF_REPORT_NOT_TRANSFERABLE");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        srm.setStatus("TRANSFERRED");
        srm.setTransferredPlanItemId(planItemId);
        srm.setUpdater(claims.getSubject());
        srm.setUpdateTime(now);
        selfReportedMedicationMapper.update(srm);
        addWorkOrderEvent(tenantId, srm.getWorkOrderId(), "PHARMACIST_REPLY",
                "PHARMACIST", pharmacist.getId(), "已将自报药物转入正式用药计划：" + srm.getDrugName(), now);
    }

    private Long resolveTenantId(JwtClaims claims) {
        return claims.getTenantId() != null ? claims.getTenantId() : DEFAULT_TENANT_ID;
    }

    private SelfReportedMedication requireById(Long tenantId, Long id) {
        SelfReportedMedication srm = selfReportedMedicationMapper.selectById(tenantId, id);
        if (srm == null) {
            throw new BusinessException(ErrorCode.HELP_ORDER_NOT_FOUND.getCode(), ErrorCode.HELP_ORDER_NOT_FOUND.getMessage());
        }
        return srm;
    }

    private void addWorkOrderEvent(Long tenantId, Long workOrderId, String eventType,
                                    String operatorType, Long operatorId, String content, LocalDateTime now) {
        if (workOrderId == null) {
            return;
        }
        HelpOrder order = helpOrderMapper.selectById(tenantId, workOrderId);
        if (order == null) {
            return;
        }
        HelpOrderEvent event = new HelpOrderEvent();
        event.setHelpOrderId(workOrderId);
        event.setEventType(eventType);
        event.setFromStatus(order.getStatus());
        event.setToStatus(order.getStatus());
        event.setOperatorType(operatorType);
        event.setOperatorId(operatorId == null ? 0L : operatorId);
        event.setContent(content);
        event.setAttachmentFileAssetId(null);
        event.setMetadataJson(null);
        event.setCreator(OPERATOR_TAG);
        event.setCreateTime(now);
        event.setUpdater(OPERATOR_TAG);
        event.setUpdateTime(now);
        event.setDeleted(false);
        event.setTenantId(tenantId);
        helpOrderEventMapper.insert(event);
    }

    private SelfReportedMedicationDTO toDto(SelfReportedMedication srm) {
        SelfReportedMedicationDTO dto = new SelfReportedMedicationDTO();
        dto.setId(srm.getId());
        dto.setPatientId(srm.getPatientId());
        dto.setDrugName(srm.getDrugName());
        dto.setDosage(srm.getDosage());
        dto.setStatus(srm.getStatus());
        dto.setNotes(srm.getNotes());
        dto.setOperatorType(srm.getOperatorType());
        dto.setOperatorAccountId(srm.getOperatorAccountId());
        dto.setWorkOrderId(srm.getWorkOrderId());
        dto.setTransferredPlanItemId(srm.getTransferredPlanItemId());
        dto.setCreatedAt(srm.getCreateTime() == null ? null
                : srm.getCreateTime().toInstant(ZoneOffset.UTC).toString());
        return dto;
    }
}
