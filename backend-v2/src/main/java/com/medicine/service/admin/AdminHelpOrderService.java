package com.medicine.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.helporder.HelpOrderAttachmentDTO;
import com.medicine.api.dto.helporder.HelpOrderCloseRequest;
import com.medicine.api.dto.helporder.HelpOrderDTO;
import com.medicine.api.dto.helporder.HelpOrderDoctorDecisionRequest;
import com.medicine.api.dto.helporder.HelpOrderEscalateRequest;
import com.medicine.api.dto.helporder.HelpOrderRejectRequest;
import com.medicine.api.dto.helporder.HelpOrderReplyRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.common.model.PageResult;
import com.medicine.domain.file.FileAsset;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.helporder.HelpOrderEvent;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.DoctorProfile;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.repository.file.FileAssetMapper;
import com.medicine.repository.helporder.HelpOrderEventAttachmentMapper;
import com.medicine.repository.helporder.HelpOrderEventMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.identity.AppAccountMapper;
import com.medicine.repository.identity.DoctorProfileMapper;
import com.medicine.repository.identity.PatientMapper;
import com.medicine.repository.identity.PharmacistProfileMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import com.medicine.service.helporder.HelpOrderVoiceTranscriptionService;
import com.medicine.service.notification.NotificationDispatchService;
import com.medicine.service.storage.S3CompatibleObjectStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AdminHelpOrderService {

    private static final Duration HELP_ORDER_DOWNLOAD_TTL = Duration.ofMinutes(15);

    private final HelpOrderMapper helpOrderMapper;
    private final HelpOrderEventMapper helpOrderEventMapper;
    private final HelpOrderEventAttachmentMapper helpOrderEventAttachmentMapper;
    private final FileAssetMapper fileAssetMapper;
    private final S3CompatibleObjectStorage objectStorage;
    private final NotificationDispatchService notificationDispatchService;
    private final ProfessionalIdentityService professionalIdentityService;
    private final PharmacistProfileMapper pharmacistProfileMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final PatientMapper patientMapper;
    private final AppAccountMapper appAccountMapper;
    private final ObjectMapper objectMapper;
    private final HelpOrderVoiceTranscriptionService voiceTranscriptionService;

    public AdminHelpOrderService(
            HelpOrderMapper helpOrderMapper,
            HelpOrderEventMapper helpOrderEventMapper,
            HelpOrderEventAttachmentMapper helpOrderEventAttachmentMapper,
            FileAssetMapper fileAssetMapper,
            S3CompatibleObjectStorage objectStorage,
            NotificationDispatchService notificationDispatchService,
            ProfessionalIdentityService professionalIdentityService,
            PharmacistProfileMapper pharmacistProfileMapper,
            DoctorProfileMapper doctorProfileMapper,
            PatientMapper patientMapper,
            AppAccountMapper appAccountMapper,
            ObjectMapper objectMapper,
            HelpOrderVoiceTranscriptionService voiceTranscriptionService
    ) {
        this.helpOrderMapper = helpOrderMapper;
        this.helpOrderEventMapper = helpOrderEventMapper;
        this.helpOrderEventAttachmentMapper = helpOrderEventAttachmentMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.objectStorage = objectStorage;
        this.notificationDispatchService = notificationDispatchService;
        this.professionalIdentityService = professionalIdentityService;
        this.pharmacistProfileMapper = pharmacistProfileMapper;
        this.doctorProfileMapper = doctorProfileMapper;
        this.patientMapper = patientMapper;
        this.appAccountMapper = appAccountMapper;
        this.objectMapper = objectMapper;
        this.voiceTranscriptionService = voiceTranscriptionService;
    }

    public PageResult<HelpOrderDTO> list(JwtClaims claims, String status, int pageNo, int pageSize) {
        ProfessionalScope scope = resolveScope(claims);
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.max(pageSize, 1);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;
        List<String> statuses = parseStatuses(status);
        long total = helpOrderMapper.countForAdmin(claims.getTenantId(), scope.operatorType(), scope.operatorId(), statuses);
        List<HelpOrderDTO> rows = helpOrderMapper.searchForAdmin(
                        claims.getTenantId(),
                        scope.operatorType(),
                        scope.operatorId(),
                        statuses,
                        offset,
                        normalizedPageSize
                ).stream()
                .map(order -> toDto(claims.getTenantId(), order, false))
                .toList();
        return new PageResult<>(normalizedPageNo, normalizedPageSize, total, rows);
    }

    public HelpOrderDTO getDetail(JwtClaims claims, Long id) {
        ProfessionalScope scope = resolveScope(claims);
        HelpOrder order = requireOrder(claims.getTenantId(), id);
        assertVisible(scope, order);
        return toDto(claims.getTenantId(), order, true);
    }

    @Transactional
    public HelpOrderDTO claim(JwtClaims claims, Long id) {
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        HelpOrder order = requireOrder(claims.getTenantId(), id);
        ensurePharmacistCanHandle(pharmacist.getId(), order);
        if (!"SUBMITTED".equals(order.getStatus()) && !"PHARMACIST_IN_PROGRESS".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_STATUS_CONFLICT.getCode(), ErrorCode.HELP_ORDER_STATUS_CONFLICT.getMessage());
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String fromStatus = order.getStatus();
        order.setStatus("PHARMACIST_IN_PROGRESS");
        order.setCurrentHandlerType("PHARMACIST");
        order.setCurrentHandlerId(pharmacist.getId());
        if (order.getResponsiblePharmacistId() == null) {
            order.setResponsiblePharmacistId(pharmacist.getId());
        }
        if (order.getFirstRespondedAt() == null) {
            order.setFirstRespondedAt(now);
        }
        order.setUpdater("admin-help-order");
        order.setUpdateTime(now);
        helpOrderMapper.updateState(order);
        appendEvent(claims.getTenantId(), order.getId(), "ASSIGNED", fromStatus, order.getStatus(), "PHARMACIST", pharmacist.getId(), "Order claimed", now);
        createNotification(
                claims.getTenantId(),
                "PATIENT",
                order.getPatientId(),
                "HELP_ORDER_CLAIMED",
                order.getId(),
                Map.of("status", order.getStatus(), "handlerType", "PHARMACIST"),
                now
        );
        return getDetail(claims, id);
    }

    @Transactional
    public HelpOrderDTO reply(JwtClaims claims, Long id, HelpOrderReplyRequest request) {
        ProfessionalScope scope = resolveScope(claims);
        HelpOrder order = requireOrder(claims.getTenantId(), id);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String fromStatus = order.getStatus();

        if ("PHARMACIST".equals(scope.operatorType())) {
            ensurePharmacistCanHandle(scope.operatorId(), order);
            if (!"SUBMITTED".equals(order.getStatus()) && !"PHARMACIST_IN_PROGRESS".equals(order.getStatus())) {
                throw new BusinessException(ErrorCode.HELP_ORDER_STATUS_CONFLICT.getCode(), ErrorCode.HELP_ORDER_STATUS_CONFLICT.getMessage());
            }
            order.setStatus("PHARMACIST_IN_PROGRESS");
            order.setCurrentHandlerType("PHARMACIST");
            order.setCurrentHandlerId(scope.operatorId());
            if (order.getResponsiblePharmacistId() == null) {
                order.setResponsiblePharmacistId(scope.operatorId());
            }
        } else {
            ensureDoctorCanHandle(scope.operatorId(), order);
            if (!"WAITING_DOCTOR".equals(order.getStatus()) && !"DOCTOR_IN_PROGRESS".equals(order.getStatus())) {
                throw new BusinessException(ErrorCode.HELP_ORDER_STATUS_CONFLICT.getCode(), ErrorCode.HELP_ORDER_STATUS_CONFLICT.getMessage());
            }
            order.setStatus("DOCTOR_IN_PROGRESS");
            order.setCurrentHandlerType("DOCTOR");
            order.setCurrentHandlerId(scope.operatorId());
            if (order.getAssignedDoctorId() == null) {
                order.setAssignedDoctorId(scope.operatorId());
            }
        }
        if (order.getFirstRespondedAt() == null) {
            order.setFirstRespondedAt(now);
        }
        order.setUpdater("admin-help-order");
        order.setUpdateTime(now);
        helpOrderMapper.updateState(order);
        appendEvent(
                claims.getTenantId(),
                order.getId(),
                "PHARMACIST".equals(scope.operatorType()) ? "PHARMACIST_REPLY" : "DOCTOR_REPLY",
                fromStatus,
                order.getStatus(),
                scope.operatorType(),
                scope.operatorId(),
                requireText(request.getContent()),
                now
        );
        createNotification(
                claims.getTenantId(),
                "PATIENT",
                order.getPatientId(),
                "HELP_ORDER_REPLY",
                order.getId(),
                Map.of("status", order.getStatus(), "operatorType", scope.operatorType()),
                now
        );
        return getDetail(claims, id);
    }

    @Transactional
    public HelpOrderDTO escalate(JwtClaims claims, Long id, HelpOrderEscalateRequest request) {
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        HelpOrder order = requireOrder(claims.getTenantId(), id);
        ensurePharmacistCanHandle(pharmacist.getId(), order);
        if ("NEW_MEDICATION_REVIEW".equals(order.getCategory())
                || (!"SUBMITTED".equals(order.getStatus()) && !"PHARMACIST_IN_PROGRESS".equals(order.getStatus()))) {
            throw new BusinessException(ErrorCode.HELP_ORDER_STATUS_CONFLICT.getCode(), ErrorCode.HELP_ORDER_STATUS_CONFLICT.getMessage());
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String fromStatus = order.getStatus();
        order.setStatus("WAITING_DOCTOR");
        order.setCurrentHandlerType("DOCTOR");
        order.setCurrentHandlerId(request.getDoctorId());
        order.setAssignedDoctorId(request.getDoctorId());
        if (order.getResponsiblePharmacistId() == null) {
            order.setResponsiblePharmacistId(pharmacist.getId());
        }
        if (order.getFirstRespondedAt() == null) {
            order.setFirstRespondedAt(now);
        }
        order.setUpdater("admin-help-order");
        order.setUpdateTime(now);
        helpOrderMapper.updateState(order);
        appendEvent(claims.getTenantId(), order.getId(), "ESCALATED_TO_DOCTOR", fromStatus, order.getStatus(), "PHARMACIST", pharmacist.getId(), request.getNote(), now);
        createNotification(
                claims.getTenantId(),
                "DOCTOR",
                request.getDoctorId(),
                "HELP_ORDER_ESCALATED",
                order.getId(),
                Map.of("status", order.getStatus(), "patientId", order.getPatientId()),
                now
        );
        return getDetail(claims, id);
    }

    @Transactional
    public HelpOrderDTO reject(JwtClaims claims, Long id, HelpOrderRejectRequest request) {
        ProfessionalScope scope = resolveScope(claims);
        HelpOrder order = requireOrder(claims.getTenantId(), id);
        ensureCurrentHandler(scope, order);
        if ("CLOSED".equals(order.getStatus()) || "REJECTED".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_STATUS_CONFLICT.getCode(), ErrorCode.HELP_ORDER_STATUS_CONFLICT.getMessage());
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String fromStatus = order.getStatus();
        order.setStatus("REJECTED");
        order.setRejectReason(requireText(request.getReason()));
        order.setUpdater("admin-help-order");
        order.setUpdateTime(now);
        helpOrderMapper.updateState(order);
        appendEvent(claims.getTenantId(), order.getId(), "REJECTED", fromStatus, order.getStatus(), scope.operatorType(), scope.operatorId(), order.getRejectReason(), now);
        createNotification(
                claims.getTenantId(),
                "PATIENT",
                order.getPatientId(),
                "HELP_ORDER_REJECTED",
                order.getId(),
                Map.of("status", order.getStatus(), "reason", order.getRejectReason()),
                now
        );
        return getDetail(claims, id);
    }

    @Transactional
    public HelpOrderDTO close(JwtClaims claims, Long id, HelpOrderCloseRequest request) {
        ProfessionalScope scope = resolveScope(claims);
        HelpOrder order = requireOrder(claims.getTenantId(), id);
        ensureCurrentHandler(scope, order);
        if ("CLOSED".equals(order.getStatus()) || "REJECTED".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_STATUS_CONFLICT.getCode(), ErrorCode.HELP_ORDER_STATUS_CONFLICT.getMessage());
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String fromStatus = order.getStatus();
        order.setStatus("CLOSED");
        order.setClosedAt(now);
        order.setServiceNotice(blankToNull(request.getNote()));
        order.setUpdater("admin-help-order");
        order.setUpdateTime(now);
        helpOrderMapper.updateState(order);
        appendEvent(claims.getTenantId(), order.getId(), "CLOSED", fromStatus, order.getStatus(), scope.operatorType(), scope.operatorId(), request.getNote(), now);
        createNotification(
                claims.getTenantId(),
                "PATIENT",
                order.getPatientId(),
                "HELP_ORDER_CLOSED",
                order.getId(),
                Map.of("status", order.getStatus(), "surveyRequired", true),
                now
        );
        return getDetail(claims, id);
    }

    @Transactional
    public HelpOrderDTO doctorDecision(JwtClaims claims, Long id, HelpOrderDoctorDecisionRequest request) {
        ProfessionalScope scope = resolveScope(claims);
        if (!"DOCTOR".equals(scope.operatorType())) {
            throw new BusinessException(ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getCode(), ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getMessage());
        }

        HelpOrder order = requireOrder(claims.getTenantId(), id);
        ensureDoctorCanHandle(scope.operatorId(), order);
        if (!"WAITING_DOCTOR".equals(order.getStatus()) && !"DOCTOR_IN_PROGRESS".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_STATUS_CONFLICT.getCode(), ErrorCode.HELP_ORDER_STATUS_CONFLICT.getMessage());
        }

        String decisionResult = normalizeDecisionResult(request.getDecisionResult());
        String content = requireText(request.getContent());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String fromStatus = order.getStatus();
        order.setStatus("CLOSED");
        order.setCurrentHandlerType("DOCTOR");
        order.setCurrentHandlerId(scope.operatorId());
        if (order.getAssignedDoctorId() == null) {
            order.setAssignedDoctorId(scope.operatorId());
        }
        if (order.getFirstRespondedAt() == null) {
            order.setFirstRespondedAt(now);
        }
        order.setClosedAt(now);
        order.setServiceNotice(content);
        order.setUpdater("admin-help-order");
        order.setUpdateTime(now);
        helpOrderMapper.updateState(order);
        appendEvent(
                claims.getTenantId(),
                order.getId(),
                "DOCTOR_DECISION",
                fromStatus,
                order.getStatus(),
                scope.operatorType(),
                scope.operatorId(),
                content,
                Map.of("decisionResult", decisionResult),
                now
        );
        createNotification(
                claims.getTenantId(),
                "PATIENT",
                order.getPatientId(),
                "HELP_ORDER_DOCTOR_DECISION",
                order.getId(),
                Map.of("status", order.getStatus(), "decisionResult", decisionResult, "surveyRequired", true),
                now
        );
        if (order.getResponsiblePharmacistId() != null) {
            createNotification(
                    claims.getTenantId(),
                    "PHARMACIST",
                    order.getResponsiblePharmacistId(),
                    "HELP_ORDER_DOCTOR_DECISION",
                    order.getId(),
                    Map.of("status", order.getStatus(), "decisionResult", decisionResult, "patientId", order.getPatientId()),
                    now
            );
        }
        return getDetail(claims, id);
    }

    private ProfessionalScope resolveScope(JwtClaims claims) {
        if (professionalIdentityService.isPharmacist(claims)) {
            return new ProfessionalScope(claims.getTenantId(), "PHARMACIST", professionalIdentityService.getOrCreatePharmacistProfile(claims).getId());
        }
        if (professionalIdentityService.isDoctor(claims)) {
            return new ProfessionalScope(claims.getTenantId(), "DOCTOR", professionalIdentityService.getOrCreateDoctorProfile(claims).getId());
        }
        throw new BusinessException(ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getCode(), ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getMessage());
    }

    private HelpOrder requireOrder(Long tenantId, Long id) {
        HelpOrder order = helpOrderMapper.selectById(tenantId, id);
        if (order == null) {
            throw new BusinessException(ErrorCode.HELP_ORDER_NOT_FOUND.getCode(), ErrorCode.HELP_ORDER_NOT_FOUND.getMessage());
        }
        return order;
    }

    private void assertVisible(ProfessionalScope scope, HelpOrder order) {
        if ("PHARMACIST".equals(scope.operatorType())) {
            if ("SUBMITTED".equals(order.getStatus())) {
                return;
            }
            if ("PHARMACIST".equals(order.getCurrentHandlerType()) && scope.operatorId().equals(order.getCurrentHandlerId())) {
                return;
            }
            if (scope.operatorId().equals(order.getResponsiblePharmacistId())) {
                return;
            }
            if (order.getResponsiblePharmacistId() == null && order.getCurrentHandlerId() == null) {
                return;
            }
        } else if ("DOCTOR".equals(scope.operatorType())) {
            if (scope.operatorId().equals(order.getAssignedDoctorId())) {
                return;
            }
            if (order.getAssignedDoctorId() == null && ("WAITING_DOCTOR".equals(order.getStatus()) || "DOCTOR_IN_PROGRESS".equals(order.getStatus()))) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.HELP_ORDER_PERMISSION_DENIED.getCode(), ErrorCode.HELP_ORDER_PERMISSION_DENIED.getMessage());
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

    private void ensureDoctorCanHandle(Long doctorId, HelpOrder order) {
        if (order.getAssignedDoctorId() == null || doctorId.equals(order.getAssignedDoctorId())) {
            return;
        }
        if ("DOCTOR".equals(order.getCurrentHandlerType()) && doctorId.equals(order.getCurrentHandlerId())) {
            return;
        }
        throw new BusinessException(ErrorCode.HELP_ORDER_PERMISSION_DENIED.getCode(), ErrorCode.HELP_ORDER_PERMISSION_DENIED.getMessage());
    }

    private void ensureCurrentHandler(ProfessionalScope scope, HelpOrder order) {
        if (!scope.operatorType().equals(order.getCurrentHandlerType()) || !scope.operatorId().equals(order.getCurrentHandlerId())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_PERMISSION_DENIED.getCode(), ErrorCode.HELP_ORDER_PERMISSION_DENIED.getMessage());
        }
    }

    private HelpOrderDTO toDto(Long tenantId, HelpOrder order, boolean includeEvents) {
        HelpOrderDTO dto = new HelpOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setPatientId(order.getPatientId());
        dto.setPatientName(order.getPatientName());
        dto.setSubmittedByType(order.getSubmittedByType());
        dto.setSubmittedById(order.getSubmittedById());
        dto.setCategory(order.getCategory());
        dto.setTitle(order.getTitle());
        dto.setDescription(order.getDescription());
        dto.setStatus(order.getStatus());
        dto.setPriority(order.getPriority());
        dto.setRequiresOfflineEmergency(Boolean.TRUE.equals(order.getRequiresOfflineEmergency()));
        dto.setCurrentHandlerType(order.getCurrentHandlerType());
        dto.setCurrentHandlerId(order.getCurrentHandlerId());
        dto.setCurrentHandlerName(resolveOperatorName(tenantId, order.getCurrentHandlerType(), order.getCurrentHandlerId()));
        dto.setResponsiblePharmacistId(order.getResponsiblePharmacistId());
        dto.setAssignedDoctorId(order.getAssignedDoctorId());
        dto.setAssignedDoctorName(resolveOperatorName(tenantId, "DOCTOR", order.getAssignedDoctorId()));
        dto.setFirstRespondedAt(formatDateTime(order.getFirstRespondedAt()));
        dto.setClosedAt(formatDateTime(order.getClosedAt()));
        dto.setRejectReason(order.getRejectReason());
        dto.setServiceNotice(order.getServiceNotice());
        dto.setCreatedAt(formatDateTime(order.getCreateTime()));
        if (includeEvents) {
            dto.setPatientContext(buildPatientContext(tenantId, order.getPatientId(), order.getPatientName()));
        }
        if (includeEvents) {
            dto.setEvents(helpOrderEventMapper.selectByHelpOrderId(tenantId, order.getId()).stream().map(event -> toEventDto(tenantId, event)).toList());
        }
        return dto;
    }

    private HelpOrderDTO.EventDTO toEventDto(Long tenantId, HelpOrderEvent event) {
        HelpOrderDTO.EventDTO dto = new HelpOrderDTO.EventDTO();
        dto.setId(event.getId());
        dto.setEventType(event.getEventType());
        dto.setFromStatus(event.getFromStatus());
        dto.setToStatus(event.getToStatus());
        dto.setOperatorType(event.getOperatorType());
        dto.setOperatorId(event.getOperatorId());
        dto.setOperatorName(resolveOperatorName(tenantId, event.getOperatorType(), event.getOperatorId()));
        dto.setContent(event.getContent());
        dto.setAttachmentFileAssetId(event.getAttachmentFileAssetId());
        dto.setAttachments(loadAttachmentDtos(tenantId, event));
        dto.setDecisionResult(extractDecisionResult(event.getMetadataJson()));
        dto.setEventAt(formatDateTime(event.getCreateTime()));
        return dto;
    }

    private HelpOrderDTO.PatientContextDTO buildPatientContext(Long tenantId, Long patientId, String fallbackName) {
        HelpOrderDTO.PatientContextDTO dto = new HelpOrderDTO.PatientContextDTO();
        Patient patient = patientMapper.selectByIdAndTenant(tenantId, patientId);
        if (patient == null) {
            dto.setName(fallbackName);
            return dto;
        }
        dto.setName(patient.getName());
        dto.setGender(patient.getGender());
        dto.setAge(patient.getAge());
        dto.setPrimaryDiseaseName(patient.getPrimaryDiseaseName());
        dto.setDrugAllergy(patient.getDrugAllergy());
        dto.setFoodAllergy(patient.getFoodAllergy());
        Map<String, Object> history = parseJsonToMap(patient.getMedicalHistoryJson());
        Object layer2 = history.get("layer2");
        if (layer2 instanceof Map<?, ?> layer2Map) {
            dto.setMedicalHistorySummary(asText(layer2Map.get("medicalHistory")));
            dto.setCurrentMedicationSummary(asText(layer2Map.get("currentMedication")));
        } else {
            dto.setMedicalHistorySummary(asText(history.get("medicalHistory")));
            dto.setCurrentMedicationSummary(asText(history.get("currentMedication")));
        }
        return dto;
    }

    private List<HelpOrderAttachmentDTO> loadAttachmentDtos(Long tenantId, HelpOrderEvent event) {
        List<HelpOrderAttachmentDTO> attachments = helpOrderEventAttachmentMapper.selectDtosByEventId(tenantId, event.getId());
        if (attachments.isEmpty() && event.getAttachmentFileAssetId() != null) {
            FileAsset legacyFile = fileAssetMapper.selectByIdAndTenant(tenantId, event.getAttachmentFileAssetId());
            if (legacyFile == null) {
                return List.of();
            }
            HelpOrderAttachmentDTO dto = new HelpOrderAttachmentDTO();
            dto.setId(null);
            dto.setFileAssetId(legacyFile.getId());
            dto.setAttachmentType("IMAGE");
            dto.setOriginalFilename(legacyFile.getOriginalFilename());
            dto.setMimeType(legacyFile.getMimeType());
            dto.setSizeBytes(legacyFile.getSizeBytes());
            dto.setDownloadUrl(objectStorage.createPresignedDownloadUrl(legacyFile.getObjectKey(), HELP_ORDER_DOWNLOAD_TTL));
            return List.of(dto);
        }
        attachments.forEach(attachment -> attachment.setDownloadUrl(
                objectStorage.createPresignedDownloadUrl(attachment.getDownloadUrl(), HELP_ORDER_DOWNLOAD_TTL)
        ));
        attachments.forEach(attachment -> voiceTranscriptionService.attachTranscript(tenantId, attachment));
        return attachments;
    }

    private String resolveOperatorName(Long tenantId, String operatorType, Long operatorId) {
        if (!StringUtils.hasText(operatorType) || operatorId == null || operatorId <= 0) {
            return null;
        }
        if ("PHARMACIST".equalsIgnoreCase(operatorType)) {
            PharmacistProfile profile = pharmacistProfileMapper.selectById(tenantId, operatorId);
            return profile == null ? null : profile.getRealName();
        }
        if ("DOCTOR".equalsIgnoreCase(operatorType)) {
            DoctorProfile profile = doctorProfileMapper.selectById(tenantId, operatorId);
            return profile == null ? null : profile.getRealName();
        }
        if ("PATIENT".equalsIgnoreCase(operatorType) || "FAMILY".equalsIgnoreCase(operatorType)) {
            AppAccount account = appAccountMapper.selectByIdAndTenant(tenantId, operatorId);
            if (account == null) {
                return null;
            }
            return StringUtils.hasText(account.getDisplayName()) ? account.getDisplayName() : account.getMobile();
        }
        return "SYSTEM";
    }

    private void appendEvent(
            Long tenantId,
            Long helpOrderId,
            String eventType,
            String fromStatus,
            String toStatus,
            String operatorType,
            Long operatorId,
            String content,
            LocalDateTime now
    ) {
        appendEvent(tenantId, helpOrderId, eventType, fromStatus, toStatus, operatorType, operatorId, content, null, now);
    }

    private void appendEvent(
            Long tenantId,
            Long helpOrderId,
            String eventType,
            String fromStatus,
            String toStatus,
            String operatorType,
            Long operatorId,
            String content,
            Map<String, Object> metadata,
            LocalDateTime now
    ) {
        HelpOrderEvent event = new HelpOrderEvent();
        event.setHelpOrderId(helpOrderId);
        event.setEventType(eventType);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setOperatorType(operatorType);
        event.setOperatorId(operatorId);
        event.setContent(blankToNull(content));
        event.setAttachmentFileAssetId(null);
        event.setMetadataJson(metadata == null ? null : writeJson(metadata));
        applyAudit(event, tenantId, "admin-help-order", now);
        helpOrderEventMapper.insert(event);
    }

    private void createNotification(
            Long tenantId,
            String targetType,
            Long targetId,
            String businessType,
            Long businessId,
            Map<String, Object> payload,
            LocalDateTime now
    ) {
        notificationDispatchService.createAndDispatch(
                tenantId,
                "admin-help-order",
                targetType,
                targetId,
                businessType,
                businessId,
                payload,
                now
        );
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (Exception ex) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            Object parsed = objectMapper.readValue(json, Map.class);
            if (parsed instanceof Map<?, ?> map) {
                return map.entrySet().stream()
                        .filter(entry -> entry.getKey() != null)
                        .collect(java.util.stream.Collectors.toMap(
                                entry -> String.valueOf(entry.getKey()),
                                Map.Entry::getValue,
                                (left, right) -> right
                        ));
            }
        } catch (Exception ignored) {
            return Map.of();
        }
        return Map.of();
    }

    private String extractDecisionResult(String metadataJson) {
        Object value = parseJsonToMap(metadataJson).get("decisionResult");
        return asText(value);
    }

    private String asText(Object value) {
        if (value == null) {
            return null;
        }
        String text = Objects.toString(value, "").trim();
        return text.isEmpty() ? null : text;
    }

    private String normalizeDecisionResult(String value) {
        String normalized = requireText(value).toUpperCase();
        if (!"ADOPTED".equals(normalized) && !"NOT_ADOPTED".equals(normalized)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        return normalized;
    }

    private String requireText(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private List<String> parseStatuses(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }

    private void applyAudit(com.medicine.domain.common.BaseEntity entity, Long tenantId, String operator, LocalDateTime now) {
        entity.setCreator(operator);
        entity.setCreateTime(now);
        entity.setUpdater(operator);
        entity.setUpdateTime(now);
        entity.setDeleted(false);
        entity.setTenantId(tenantId);
    }

    private record ProfessionalScope(Long tenantId, String operatorType, Long operatorId) {
    }
}
