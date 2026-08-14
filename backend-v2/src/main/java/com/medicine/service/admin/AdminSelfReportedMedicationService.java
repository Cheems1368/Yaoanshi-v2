package com.medicine.service.admin;

import com.medicine.api.dto.medication.SelfReportedMedicationDTO;
import com.medicine.api.dto.medication.TransferPrefillDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.helporder.HelpOrderEvent;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.domain.medication.MedicationPlan;
import com.medicine.domain.medication.MedicationPlanItem;
import com.medicine.domain.medication.SelfReportedMedication;
import com.medicine.repository.helporder.HelpOrderEventMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.medication.MedicationPlanItemMapper;
import com.medicine.repository.medication.MedicationPlanMapper;
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
import java.util.Objects;

@Service
public class AdminSelfReportedMedicationService {

    private static final long DEFAULT_TENANT_ID = 1L;
    private static final String OPERATOR_TAG = "admin-self-report";

    private final SelfReportedMedicationMapper selfReportedMedicationMapper;
    private final HelpOrderMapper helpOrderMapper;
    private final HelpOrderEventMapper helpOrderEventMapper;
    private final MedicationPlanItemMapper medicationPlanItemMapper;
    private final MedicationPlanMapper medicationPlanMapper;
    private final ProfessionalIdentityService professionalIdentityService;
    private final NotificationDispatchService notificationDispatchService;

    public AdminSelfReportedMedicationService(
            SelfReportedMedicationMapper selfReportedMedicationMapper,
            HelpOrderMapper helpOrderMapper,
            HelpOrderEventMapper helpOrderEventMapper,
            MedicationPlanItemMapper medicationPlanItemMapper,
            MedicationPlanMapper medicationPlanMapper,
            ProfessionalIdentityService professionalIdentityService,
            NotificationDispatchService notificationDispatchService
    ) {
        this.selfReportedMedicationMapper = selfReportedMedicationMapper;
        this.helpOrderMapper = helpOrderMapper;
        this.helpOrderEventMapper = helpOrderEventMapper;
        this.medicationPlanItemMapper = medicationPlanItemMapper;
        this.medicationPlanMapper = medicationPlanMapper;
        this.professionalIdentityService = professionalIdentityService;
        this.notificationDispatchService = notificationDispatchService;
    }

    public List<SelfReportedMedicationDTO> listForWorkOrder(JwtClaims claims, Long workOrderId) {
        WorkOrderAccess access = requireWorkOrderAccess(claims, workOrderId);
        return selfReportedMedicationMapper
                .selectByWorkOrderId(access.tenantId(), workOrderId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public SelfReportedMedicationDTO acknowledge(JwtClaims claims, Long workOrderId, Long id) {
        WorkOrderAccess access = requireWorkOrderAccess(claims, workOrderId);
        SelfReportedMedication srm = requireForWorkOrder(access, id);
        if (!"PENDING".equals(srm.getStatus())) {
            throw new BusinessException(400, "SELF_REPORT_NOT_PENDING");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        srm.setStatus("ACTIVE");
        srm.setUpdater(claims.getSubject());
        srm.setUpdateTime(now);
        selfReportedMedicationMapper.update(srm);
        addWorkOrderEvent(access.tenantId(), access.order(), "PHARMACIST_REPLY",
                "PHARMACIST", access.pharmacistId(), "已知晓并确认自报药物：" + srm.getDrugName(), now);
        notificationDispatchService.createAndDispatch(
                access.tenantId(), OPERATOR_TAG,
                "PATIENT", srm.getPatientId(),
                "SELF_MEDICATION_ACKNOWLEDGED", srm.getId(),
                Map.of("drugName", srm.getDrugName() != null ? srm.getDrugName() : ""), now);
        return toDto(srm);
    }

    @Transactional
    public SelfReportedMedicationDTO reject(JwtClaims claims, Long workOrderId, Long id, String reason) {
        WorkOrderAccess access = requireWorkOrderAccess(claims, workOrderId);
        SelfReportedMedication srm = requireForWorkOrder(access, id);
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
        addWorkOrderEvent(access.tenantId(), access.order(), "PHARMACIST_REPLY",
                "PHARMACIST", access.pharmacistId(), eventContent, now);
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("drugName", srm.getDrugName() != null ? srm.getDrugName() : "");
        if (StringUtils.hasText(reason)) {
            payload.put("reason", reason.trim());
        }
        notificationDispatchService.createAndDispatch(
                access.tenantId(), OPERATOR_TAG,
                "PATIENT", srm.getPatientId(),
                "SELF_MEDICATION_REJECTED", srm.getId(),
                payload, now);
        return toDto(srm);
    }

    public TransferPrefillDTO getTransferPrefill(JwtClaims claims, Long workOrderId, Long id) {
        WorkOrderAccess access = requireWorkOrderAccess(claims, workOrderId);
        SelfReportedMedication srm = requireForWorkOrder(access, id);
        TransferPrefillDTO dto = new TransferPrefillDTO();
        dto.setDrugName(srm.getDrugName() != null ? srm.getDrugName() : "");
        dto.setDosage(srm.getDosage() != null ? srm.getDosage() : "");
        dto.setPatientId(srm.getPatientId());
        dto.setSelfReportedMedicationId(srm.getId());
        return dto;
    }

    @Transactional
    public void markTransferred(JwtClaims claims, Long workOrderId, Long id, Long planItemId) {
        WorkOrderAccess access = requireWorkOrderAccess(claims, workOrderId);
        SelfReportedMedication srm = requireForWorkOrder(access, id);
        if (!"ACTIVE".equals(srm.getStatus()) && !"PENDING".equals(srm.getStatus())) {
            throw new BusinessException(400, "SELF_REPORT_NOT_TRANSFERABLE");
        }
        MedicationPlanItem planItem = medicationPlanItemMapper.selectById(access.tenantId(), planItemId);
        MedicationPlan plan = planItem == null ? null : medicationPlanMapper.selectById(access.tenantId(), planItem.getPlanId());
        if (plan == null) {
            throw new BusinessException(ErrorCode.MED_PLAN_NOT_FOUND.getCode(), ErrorCode.MED_PLAN_NOT_FOUND.getMessage());
        }
        if (!Objects.equals(plan.getPatientId(), srm.getPatientId())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_PERMISSION_DENIED.getCode(), ErrorCode.HELP_ORDER_PERMISSION_DENIED.getMessage());
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        srm.setStatus("TRANSFERRED");
        srm.setTransferredPlanItemId(planItemId);
        srm.setUpdater(claims.getSubject());
        srm.setUpdateTime(now);
        selfReportedMedicationMapper.update(srm);
        addWorkOrderEvent(access.tenantId(), access.order(), "PHARMACIST_REPLY",
                "PHARMACIST", access.pharmacistId(), "已将自报药物转入正式用药计划：" + srm.getDrugName(), now);
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

    private WorkOrderAccess requireWorkOrderAccess(JwtClaims claims, Long workOrderId) {
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        Long tenantId = resolveTenantId(claims);
        HelpOrder order = helpOrderMapper.selectById(tenantId, workOrderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.HELP_ORDER_NOT_FOUND.getCode(), ErrorCode.HELP_ORDER_NOT_FOUND.getMessage());
        }
        ensurePharmacistCanHandle(pharmacist.getId(), order);
        return new WorkOrderAccess(tenantId, pharmacist.getId(), order);
    }

    private SelfReportedMedication requireForWorkOrder(WorkOrderAccess access, Long id) {
        SelfReportedMedication srm = requireById(access.tenantId(), id);
        if (!Objects.equals(srm.getWorkOrderId(), access.order().getId())
                || !Objects.equals(srm.getPatientId(), access.order().getPatientId())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_PERMISSION_DENIED.getCode(), ErrorCode.HELP_ORDER_PERMISSION_DENIED.getMessage());
        }
        return srm;
    }

    private void ensurePharmacistCanHandle(Long pharmacistId, HelpOrder order) {
        if ("SUBMITTED".equals(order.getStatus())) {
            return;
        }
        if ("PHARMACIST".equals(order.getCurrentHandlerType()) && pharmacistId.equals(order.getCurrentHandlerId())) {
            return;
        }
        if (pharmacistId.equals(order.getResponsiblePharmacistId())) {
            return;
        }
        if (order.getResponsiblePharmacistId() == null && order.getCurrentHandlerId() == null) {
            return;
        }
        throw new BusinessException(ErrorCode.HELP_ORDER_PERMISSION_DENIED.getCode(), ErrorCode.HELP_ORDER_PERMISSION_DENIED.getMessage());
    }

    private void addWorkOrderEvent(Long tenantId, HelpOrder order, String eventType,
                                    String operatorType, Long operatorId, String content, LocalDateTime now) {
        HelpOrderEvent event = new HelpOrderEvent();
        event.setHelpOrderId(order.getId());
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

    private record WorkOrderAccess(Long tenantId, Long pharmacistId, HelpOrder order) {
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
