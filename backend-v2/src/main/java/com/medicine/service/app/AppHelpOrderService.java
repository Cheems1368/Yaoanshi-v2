package com.medicine.service.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.helporder.HelpOrderDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.common.model.PageResult;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.helporder.HelpOrderEvent;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.DoctorProfile;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.repository.helporder.HelpOrderEventMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.identity.AppAccountMapper;
import com.medicine.repository.identity.DoctorProfileMapper;
import com.medicine.repository.identity.PharmacistProfileMapper;
import com.medicine.service.auth.AppPatientService;
import com.medicine.service.notification.NotificationDispatchService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AppHelpOrderService {

    private static final long DEFAULT_TENANT_ID = 1L;

    private final HelpOrderMapper helpOrderMapper;
    private final HelpOrderEventMapper helpOrderEventMapper;
    private final NotificationDispatchService notificationDispatchService;
    private final AppPatientService appPatientService;
    private final AppAccountMapper appAccountMapper;
    private final PharmacistProfileMapper pharmacistProfileMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final ObjectMapper objectMapper;

    public AppHelpOrderService(
            HelpOrderMapper helpOrderMapper,
            HelpOrderEventMapper helpOrderEventMapper,
            NotificationDispatchService notificationDispatchService,
            AppPatientService appPatientService,
            AppAccountMapper appAccountMapper,
            PharmacistProfileMapper pharmacistProfileMapper,
            DoctorProfileMapper doctorProfileMapper,
            ObjectMapper objectMapper
    ) {
        this.helpOrderMapper = helpOrderMapper;
        this.helpOrderEventMapper = helpOrderEventMapper;
        this.notificationDispatchService = notificationDispatchService;
        this.appPatientService = appPatientService;
        this.appAccountMapper = appAccountMapper;
        this.pharmacistProfileMapper = pharmacistProfileMapper;
        this.doctorProfileMapper = doctorProfileMapper;
        this.objectMapper = objectMapper;
    }

    public HelpOrderDTO create(String subject, HelpOrderDTO request) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        Patient patient = context.patient();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        HelpOrder order = new HelpOrder();
        order.setOrderNo(generateOrderNo());
        order.setPatientId(patient.getId());
        order.setSubmittedByType(context.operatorType());
        order.setSubmittedById(context.appAccount().getId());
        order.setCategory(normalizeCategory(request.getCategory()));
        order.setTitle(requireText(request.getTitle()));
        order.setDescription(requireText(request.getDescription()));
        order.setStatus("SUBMITTED");
        order.setPriority(normalizePriority(request.getPriority()));
        order.setRequiresOfflineEmergency(Boolean.TRUE.equals(request.getRequiresOfflineEmergency()));
        order.setCurrentHandlerType(null);
        order.setCurrentHandlerId(null);
        order.setResponsiblePharmacistId(patient.getBoundByPharmacistId());
        order.setAssignedDoctorId(null);
        order.setFirstRespondedAt(null);
        order.setClosedAt(null);
        order.setRejectReason(null);
        order.setServiceNotice(blankToNull(request.getServiceNotice()));
        applyAudit(order, "app-help-order", now);
        helpOrderMapper.insert(order);

        appendEvent(order.getId(), "SUBMITTED", null, "SUBMITTED", context.operatorType(), context.appAccount().getId(), order.getDescription(), now);
        createNotification(
                "PHARMACIST",
                patient.getBoundByPharmacistId(),
                "HELP_ORDER_SUBMITTED",
                order.getId(),
                Map.of("orderNo", order.getOrderNo(), "patientId", patient.getId(), "category", order.getCategory()),
                now
        );
        return getDetail(subject, order.getId());
    }

    public PageResult<HelpOrderDTO> list(String subject, String status, String category, int pageNo, int pageSize) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.max(pageSize, 1);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;
        long total = helpOrderMapper.countForApp(DEFAULT_TENANT_ID, context.patient().getId(), blankToNull(status), blankToNull(category));
        List<HelpOrderDTO> rows = helpOrderMapper.searchForApp(
                        DEFAULT_TENANT_ID,
                        context.patient().getId(),
                        blankToNull(status),
                        blankToNull(category),
                        offset,
                        normalizedPageSize
                ).stream()
                .map(order -> toDto(order, false))
                .toList();
        return new PageResult<>(normalizedPageNo, normalizedPageSize, total, rows);
    }

    public HelpOrderDTO getDetail(String subject, Long id) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        HelpOrder order = helpOrderMapper.selectById(DEFAULT_TENANT_ID, id);
        if (order == null || !context.patient().getId().equals(order.getPatientId())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_NOT_FOUND.getCode(), ErrorCode.HELP_ORDER_NOT_FOUND.getMessage());
        }
        return toDto(order, true);
    }

    private HelpOrderDTO toDto(HelpOrder order, boolean includeEvents) {
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
        dto.setCurrentHandlerName(resolveOperatorName(order.getCurrentHandlerType(), order.getCurrentHandlerId()));
        dto.setResponsiblePharmacistId(order.getResponsiblePharmacistId());
        dto.setAssignedDoctorId(order.getAssignedDoctorId());
        dto.setAssignedDoctorName(resolveOperatorName("DOCTOR", order.getAssignedDoctorId()));
        dto.setFirstRespondedAt(formatDateTime(order.getFirstRespondedAt()));
        dto.setClosedAt(formatDateTime(order.getClosedAt()));
        dto.setRejectReason(order.getRejectReason());
        dto.setServiceNotice(order.getServiceNotice());
        dto.setCreatedAt(formatDateTime(order.getCreateTime()));
        if (includeEvents) {
            dto.setEvents(helpOrderEventMapper.selectByHelpOrderId(DEFAULT_TENANT_ID, order.getId()).stream().map(this::toEventDto).toList());
        }
        return dto;
    }

    private HelpOrderDTO.EventDTO toEventDto(HelpOrderEvent event) {
        HelpOrderDTO.EventDTO dto = new HelpOrderDTO.EventDTO();
        dto.setId(event.getId());
        dto.setEventType(event.getEventType());
        dto.setFromStatus(event.getFromStatus());
        dto.setToStatus(event.getToStatus());
        dto.setOperatorType(event.getOperatorType());
        dto.setOperatorId(event.getOperatorId());
        dto.setOperatorName(resolveOperatorName(event.getOperatorType(), event.getOperatorId()));
        dto.setContent(event.getContent());
        dto.setAttachmentFileAssetId(event.getAttachmentFileAssetId());
        dto.setEventAt(formatDateTime(event.getCreateTime()));
        return dto;
    }

    private void appendEvent(
            Long helpOrderId,
            String eventType,
            String fromStatus,
            String toStatus,
            String operatorType,
            Long operatorId,
            String content,
            LocalDateTime now
    ) {
        HelpOrderEvent event = new HelpOrderEvent();
        event.setHelpOrderId(helpOrderId);
        event.setEventType(eventType);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setOperatorType(operatorType);
        event.setOperatorId(operatorId == null ? 0L : operatorId);
        event.setContent(blankToNull(content));
        event.setAttachmentFileAssetId(null);
        event.setMetadataJson(null);
        applyAudit(event, "app-help-order", now);
        helpOrderEventMapper.insert(event);
    }

    private void createNotification(
            String targetType,
            Long targetId,
            String businessType,
            Long businessId,
            Map<String, Object> payload,
            LocalDateTime now
    ) {
        notificationDispatchService.createAndDispatch(
                DEFAULT_TENANT_ID,
                "app-help-order",
                targetType,
                targetId,
                businessType,
                businessId,
                payload,
                now
        );
    }

    private String resolveOperatorName(String operatorType, Long operatorId) {
        if (!StringUtils.hasText(operatorType) || operatorId == null || operatorId <= 0) {
            return null;
        }
        if ("PHARMACIST".equalsIgnoreCase(operatorType)) {
            PharmacistProfile profile = pharmacistProfileMapper.selectById(DEFAULT_TENANT_ID, operatorId);
            return profile == null ? null : profile.getRealName();
        }
        if ("DOCTOR".equalsIgnoreCase(operatorType)) {
            DoctorProfile profile = doctorProfileMapper.selectById(DEFAULT_TENANT_ID, operatorId);
            return profile == null ? null : profile.getRealName();
        }
        if ("PATIENT".equalsIgnoreCase(operatorType) || "FAMILY".equalsIgnoreCase(operatorType)) {
            AppAccount account = appAccountMapper.selectByIdAndTenant(DEFAULT_TENANT_ID, operatorId);
            if (account == null) {
                return null;
            }
            return StringUtils.hasText(account.getDisplayName()) ? account.getDisplayName() : account.getMobile();
        }
        return "SYSTEM";
    }

    private String generateOrderNo() {
        return "HO" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private String normalizeCategory(String value) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            return "OTHER";
        }
        return normalized.toUpperCase();
    }

    private String normalizePriority(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : "NORMAL";
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

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (Exception ex) {
            return "{}";
        }
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
