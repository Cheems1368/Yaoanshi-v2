package com.medicine.service.app;

import com.medicine.api.dto.medication.SelfReportedMedicationCreateRequest;
import com.medicine.api.dto.medication.SelfReportedMedicationDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.helporder.HelpOrderEvent;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.medication.SelfReportedMedication;
import com.medicine.repository.helporder.HelpOrderEventMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.medication.SelfReportedMedicationMapper;
import com.medicine.service.auth.AppPatientService;
import com.medicine.service.notification.NotificationDispatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AppSelfReportedMedicationService {

    private static final long DEFAULT_TENANT_ID = 1L;
    private static final String OPERATOR_TAG = "app-self-report";

    private final SelfReportedMedicationMapper selfReportedMedicationMapper;
    private final HelpOrderMapper helpOrderMapper;
    private final HelpOrderEventMapper helpOrderEventMapper;
    private final NotificationDispatchService notificationDispatchService;
    private final AppPatientService appPatientService;

    public AppSelfReportedMedicationService(
            SelfReportedMedicationMapper selfReportedMedicationMapper,
            HelpOrderMapper helpOrderMapper,
            HelpOrderEventMapper helpOrderEventMapper,
            NotificationDispatchService notificationDispatchService,
            AppPatientService appPatientService
    ) {
        this.selfReportedMedicationMapper = selfReportedMedicationMapper;
        this.helpOrderMapper = helpOrderMapper;
        this.helpOrderEventMapper = helpOrderEventMapper;
        this.notificationDispatchService = notificationDispatchService;
        this.appPatientService = appPatientService;
    }

    /**
     * Creates one or more self-reported medication records (one per drug entry) and a single
     * NEW_MEDICATION_REVIEW work order covering all of them. The work order priority is URGENT
     * if any HIGH-severity DDI alert was detected (caller is responsible for screening before
     * calling this method and passing the resolved priority).
     */
    @Transactional
    public List<SelfReportedMedicationDTO> create(
            String subject,
            SelfReportedMedicationCreateRequest request,
            String resolvedPriority
    ) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        Patient patient = context.patient();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        if (request == null || request.getDrugs() == null || request.getDrugs().isEmpty()) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        // 1. Create a single work order for this batch submission
        HelpOrder order = new HelpOrder();
        order.setOrderNo(generateOrderNo());
        order.setPatientId(patient.getId());
        order.setSubmittedByType(context.operatorType());
        order.setSubmittedById(context.appAccount().getId());
        order.setCategory("NEW_MEDICATION_REVIEW");
        order.setTitle(buildTitle(request));
        order.setDescription(StringUtils.hasText(request.getNotes()) ? request.getNotes().trim() : buildTitle(request));
        order.setStatus("SUBMITTED");
        order.setPriority(StringUtils.hasText(resolvedPriority) ? resolvedPriority.toUpperCase() : "NORMAL");
        order.setRequiresOfflineEmergency(false);
        order.setCurrentHandlerType(null);
        order.setCurrentHandlerId(null);
        order.setResponsiblePharmacistId(patient.getBoundByPharmacistId());
        order.setAssignedDoctorId(null);
        order.setFirstRespondedAt(null);
        order.setClosedAt(null);
        order.setRejectReason(null);
        order.setServiceNotice(null);
        applyAudit(order, OPERATOR_TAG, now);
        helpOrderMapper.insert(order);

        appendEvent(order.getId(), "SUBMITTED", null, "SUBMITTED",
                context.operatorType(), context.appAccount().getId(), order.getDescription(), now);

        notificationDispatchService.createAndDispatch(
                DEFAULT_TENANT_ID, OPERATOR_TAG,
                "PHARMACIST", patient.getBoundByPharmacistId(),
                "NEW_MEDICATION_REVIEW", order.getId(),
                Map.of("orderNo", order.getOrderNo(),
                        "patientId", patient.getId(),
                        "drugCount", request.getDrugs().size()),
                now
        );

        // 2. Create one self_reported_medication record per drug
        List<SelfReportedMedication> created = request.getDrugs().stream()
                .filter(d -> StringUtils.hasText(d.getDrugName()))
                .map(entry -> {
                    SelfReportedMedication srm = new SelfReportedMedication();
                    srm.setPatientId(patient.getId());
                    srm.setDrugName(entry.getDrugName().trim());
                    srm.setDosage(StringUtils.hasText(entry.getDosage()) ? entry.getDosage().trim() : null);
                    srm.setStatus("PENDING");
                    srm.setNotes(StringUtils.hasText(request.getNotes()) ? request.getNotes().trim() : null);
                    srm.setOperatorType(context.operatorType());
                    srm.setOperatorAccountId(context.appAccount().getId());
                    srm.setWorkOrderId(order.getId());
                    applyAudit(srm, OPERATOR_TAG, now);
                    selfReportedMedicationMapper.insert(srm);
                    return srm;
                })
                .toList();

        return created.stream().map(this::toDto).toList();
    }

    public List<SelfReportedMedicationDTO> list(String subject) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        return selfReportedMedicationMapper
                .selectByPatientId(DEFAULT_TENANT_ID, context.patient().getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void withdraw(String subject, Long id) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        SelfReportedMedication srm = selfReportedMedicationMapper.selectById(DEFAULT_TENANT_ID, id);
        if (srm == null || !context.patient().getId().equals(srm.getPatientId())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_NOT_FOUND.getCode(), ErrorCode.HELP_ORDER_NOT_FOUND.getMessage());
        }
        if (!"PENDING".equals(srm.getStatus())) {
            throw new BusinessException(400, "SELF_REPORT_NOT_WITHDRAWABLE");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        srm.setStatus("STOPPED");
        srm.setUpdater(OPERATOR_TAG);
        srm.setUpdateTime(now);
        selfReportedMedicationMapper.update(srm);

        // Auto-close the work order if all medications in the batch are now stopped.
        if (srm.getWorkOrderId() != null) {
            int remaining = selfReportedMedicationMapper.countActiveByWorkOrderId(DEFAULT_TENANT_ID, srm.getWorkOrderId());
            if (remaining == 0) {
                HelpOrder order = helpOrderMapper.selectById(DEFAULT_TENANT_ID, srm.getWorkOrderId());
                if (order != null && "SUBMITTED".equals(order.getStatus())) {
                    order.setStatus("CLOSED");
                    order.setClosedAt(now);
                    order.setRejectReason("患者已撤回全部自报药物");
                    order.setUpdater(OPERATOR_TAG);
                    order.setUpdateTime(now);
                    helpOrderMapper.updateState(order);
                    appendEvent(order.getId(), "CLOSED", "SUBMITTED", "CLOSED",
                            context.operatorType(), context.appAccount().getId(), "患者已撤回全部自报药物", now);
                }
            }
        }
    }

    @Transactional
    public void stop(String subject, Long id) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        SelfReportedMedication srm = selfReportedMedicationMapper.selectById(DEFAULT_TENANT_ID, id);
        if (srm == null || !context.patient().getId().equals(srm.getPatientId())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_NOT_FOUND.getCode(), ErrorCode.HELP_ORDER_NOT_FOUND.getMessage());
        }
        if (!"ACTIVE".equals(srm.getStatus())) {
            throw new BusinessException(400, "SELF_REPORT_NOT_STOPPABLE");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        srm.setStatus("STOPPED");
        srm.setUpdater(OPERATOR_TAG);
        srm.setUpdateTime(now);
        selfReportedMedicationMapper.update(srm);
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
        dto.setCreatedAt(srm.getCreateTime() == null ? null : srm.getCreateTime().toInstant(ZoneOffset.UTC).toString());
        return dto;
    }

    private String buildTitle(SelfReportedMedicationCreateRequest request) {
        if (request.getDrugs().size() == 1) {
            return "新增自报药物：" + request.getDrugs().get(0).getDrugName().trim();
        }
        return "新增自报药物（" + request.getDrugs().size() + "种）：" +
                request.getDrugs().stream()
                        .filter(d -> StringUtils.hasText(d.getDrugName()))
                        .map(d -> d.getDrugName().trim())
                        .limit(3)
                        .reduce((a, b) -> a + "、" + b)
                        .orElse("");
    }

    private String generateOrderNo() {
        return "NMR" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private void appendEvent(Long helpOrderId, String eventType, String fromStatus, String toStatus,
                              String operatorType, Long operatorId, String content, LocalDateTime now) {
        HelpOrderEvent event = new HelpOrderEvent();
        event.setHelpOrderId(helpOrderId);
        event.setEventType(eventType);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setOperatorType(operatorType);
        event.setOperatorId(operatorId == null ? 0L : operatorId);
        event.setContent(content);
        event.setAttachmentFileAssetId(null);
        event.setMetadataJson(null);
        applyAudit(event, OPERATOR_TAG, now);
        helpOrderEventMapper.insert(event);
    }

    private void applyAudit(com.medicine.domain.common.BaseEntity entity, String operator, LocalDateTime now) {
        entity.setCreator(operator);
        entity.setCreateTime(now);
        entity.setUpdater(operator);
        entity.setUpdateTime(now);
        entity.setDeleted(false);
        entity.setTenantId(DEFAULT_TENANT_ID);
    }
}
