package com.medicine.service.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.medication.CheckinCorrectRequest;
import com.medicine.api.dto.medication.CheckinRecordDTO;
import com.medicine.api.dto.medication.CheckinSubmitRequest;
import com.medicine.api.dto.medication.DoseTaskDTO;
import com.medicine.api.dto.medication.MedicationPlanDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.common.model.PageResult;
import com.medicine.domain.medication.MedicationCheckinRecord;
import com.medicine.domain.medication.MedicationDoseTask;
import com.medicine.domain.medication.MedicationPlan;
import com.medicine.domain.medication.MedicationPlanEvent;
import com.medicine.repository.identity.PatientFamilyBindingMapper;
import com.medicine.repository.medication.MedicationCheckinRecordMapper;
import com.medicine.repository.medication.MedicationDoseTaskMapper;
import com.medicine.repository.medication.MedicationPlanEventMapper;
import com.medicine.repository.medication.MedicationPlanItemMapper;
import com.medicine.repository.medication.MedicationPlanMapper;
import com.medicine.service.auth.AppPatientService;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
public class AppMedicationService {

    private static final long DEFAULT_TENANT_ID = 1L;
    private static final long FAMILY_REMINDER_DELAY_MINUTES = 30L;
    private static final Set<String> HANDLED_TASK_STATUSES = Set.of("COMPLETED", "MISSED", "SKIPPED", "CANCELLED");

    private final MedicationPlanMapper medicationPlanMapper;
    private final MedicationPlanItemMapper medicationPlanItemMapper;
    private final MedicationPlanEventMapper medicationPlanEventMapper;
    private final MedicationDoseTaskMapper medicationDoseTaskMapper;
    private final MedicationCheckinRecordMapper medicationCheckinRecordMapper;
    private final PatientFamilyBindingMapper patientFamilyBindingMapper;
    private final AppPatientService appPatientService;
    private final ObjectMapper objectMapper;

    public AppMedicationService(
            MedicationPlanMapper medicationPlanMapper,
            MedicationPlanItemMapper medicationPlanItemMapper,
            MedicationPlanEventMapper medicationPlanEventMapper,
            MedicationDoseTaskMapper medicationDoseTaskMapper,
            MedicationCheckinRecordMapper medicationCheckinRecordMapper,
            PatientFamilyBindingMapper patientFamilyBindingMapper,
            AppPatientService appPatientService,
            ObjectMapper objectMapper
    ) {
        this.medicationPlanMapper = medicationPlanMapper;
        this.medicationPlanItemMapper = medicationPlanItemMapper;
        this.medicationPlanEventMapper = medicationPlanEventMapper;
        this.medicationDoseTaskMapper = medicationDoseTaskMapper;
        this.medicationCheckinRecordMapper = medicationCheckinRecordMapper;
        this.patientFamilyBindingMapper = patientFamilyBindingMapper;
        this.appPatientService = appPatientService;
        this.objectMapper = objectMapper;
    }

    public MedicationPlanDTO getActivePlan(String subject) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        MedicationPlan plan = medicationPlanMapper.selectActiveByPatientId(DEFAULT_TENANT_ID, context.patient().getId());
        if (plan == null) {
            return null;
        }
        return toPlanDto(plan);
    }

    public List<DoseTaskDTO> getTodayDoseTasks(String subject, String date) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        LocalDate taskDate = StringUtils.hasText(date) ? LocalDate.parse(date) : LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"));
        int familyBindingCount = patientFamilyBindingMapper.countActiveByPatientId(DEFAULT_TENANT_ID, context.patient().getId());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return medicationDoseTaskMapper.selectTodayByPatientId(DEFAULT_TENANT_ID, context.patient().getId(), taskDate)
                .stream()
                .map(task -> toDoseTaskDto(task, familyBindingCount, now))
                .toList();
    }

    public List<DoseTaskDTO> getDoseTasks(String subject, String dateFrom, String dateTo) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        LocalDate from = parseRequiredDate(dateFrom);
        LocalDate to = parseRequiredDate(dateTo);
        if (to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > 6) {
            throw new BusinessException(400, "DOSE_TASK_DATE_RANGE_INVALID");
        }
        int familyBindingCount = patientFamilyBindingMapper.countActiveByPatientId(DEFAULT_TENANT_ID, context.patient().getId());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return medicationDoseTaskMapper.selectByPatientIdAndDateRange(DEFAULT_TENANT_ID, context.patient().getId(), from, to)
                .stream()
                .map(task -> toDoseTaskDto(task, familyBindingCount, now))
                .toList();
    }

    public PageResult<CheckinRecordDTO> getCheckins(String subject, String dateFrom, String dateTo, int pageNo, int pageSize) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        LocalDate from = StringUtils.hasText(dateFrom) ? LocalDate.parse(dateFrom) : LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")).minusDays(30);
        LocalDate to = StringUtils.hasText(dateTo) ? LocalDate.parse(dateTo) : LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"));
        List<CheckinRecordDTO> rows = medicationCheckinRecordMapper
                .selectByPatientIdAndDateRange(DEFAULT_TENANT_ID, context.patient().getId(), from, to)
                .stream()
                .map(this::toCheckinDto)
                .toList();
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.max(pageSize, 1);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;
        int toIndex = Math.min(rows.size(), offset + normalizedPageSize);
        List<CheckinRecordDTO> pageList = offset >= rows.size() ? List.of() : rows.subList(offset, toIndex);
        return new PageResult<>(normalizedPageNo, normalizedPageSize, rows.size(), pageList);
    }

    @Transactional
    public CheckinRecordDTO submitCheckin(String subject, CheckinSubmitRequest request) {
        if (request == null || request.getDoseTaskId() == null) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        String clientRequestId = normalizeClientRequestId(request.getClientRequestId());
        if (clientRequestId != null) {
            MedicationCheckinRecord existing = medicationCheckinRecordMapper.selectByClientRequestId(
                    DEFAULT_TENANT_ID, context.patient().getId(), clientRequestId
            );
            if (existing != null) {
                return toCheckinDto(existing);
            }
        }
        MedicationDoseTask task = requireDoseTask(context.patient().getId(), request.getDoseTaskId());
        if (clientRequestId != null) {
            MedicationCheckinRecord existing = medicationCheckinRecordMapper.selectByClientRequestId(
                    DEFAULT_TENANT_ID, context.patient().getId(), clientRequestId
            );
            if (existing != null) return toCheckinDto(existing);
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (task.getLockedAfter() != null && task.getLockedAfter().isBefore(now)) {
            throw new BusinessException(
                    ErrorCode.DOSE_TASK_EDIT_WINDOW_EXPIRED.getCode(),
                    ErrorCode.DOSE_TASK_EDIT_WINDOW_EXPIRED.getMessage()
            );
        }

        MedicationCheckinRecord record = new MedicationCheckinRecord();
        record.setDoseTaskId(task.getId());
        record.setPatientId(context.patient().getId());
        record.setClientRequestId(clientRequestId);
        record.setOperatorType(context.operatorType());
        record.setOperatorId(context.appAccount().getId());
        record.setResult(normalizeResult(request.getResult()));
        record.setActualTakenAt(validateActualTakenAt(record.getResult(), request.getActualTakenAt(), task, now));
        record.setNote(blankToNull(request.getNote()));
        record.setSkipReason(blankToNull(request.getSkipReason()));
        boolean correction = task.getLastEffectiveCheckinId() != null;
        String auditActor = correction ? "app-checkin-correct" : "app-checkin";
        record.setIsCorrection(correction);
        record.setSupersedesRecordId(task.getLastEffectiveCheckinId());
        record.setIsEffective(true);
        applyAudit(record, auditActor, now);
        try {
            medicationCheckinRecordMapper.insert(record);
        } catch (DataIntegrityViolationException ex) {
            MedicationCheckinRecord existing = clientRequestId == null ? null : medicationCheckinRecordMapper.selectByClientRequestIdForUpdate(
                    DEFAULT_TENANT_ID, context.patient().getId(), clientRequestId
            );
            if (existing == null) {
                throw ex;
            }
            return toCheckinDto(existing);
        }
        medicationCheckinRecordMapper.markOthersIneffectiveByDoseTaskId(
                DEFAULT_TENANT_ID, task.getId(), record.getId(), auditActor, now
        );

        task.setStatus(toTaskStatus(record.getResult()));
        task.setNotificationStatus("ACKED");
        task.setLastEffectiveCheckinId(record.getId());
        task.setUpdater(auditActor);
        task.setUpdateTime(now);
        medicationDoseTaskMapper.updateStatus(task);
        return toCheckinDto(record);
    }

    @Transactional
    public CheckinRecordDTO correctCheckin(String subject, Long checkinId, CheckinCorrectRequest request) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        String clientRequestId = normalizeClientRequestId(request == null ? null : request.getClientRequestId());
        if (clientRequestId != null) {
            MedicationCheckinRecord existing = medicationCheckinRecordMapper.selectByClientRequestId(
                    DEFAULT_TENANT_ID, context.patient().getId(), clientRequestId
            );
            if (existing != null) return toCheckinDto(existing);
        }
        MedicationCheckinRecord original = medicationCheckinRecordMapper.selectById(DEFAULT_TENANT_ID, checkinId);
        if (original == null || !context.patient().getId().equals(original.getPatientId())) {
            throw new BusinessException(ErrorCode.CHECKIN_RECORD_NOT_FOUND.getCode(), ErrorCode.CHECKIN_RECORD_NOT_FOUND.getMessage());
        }
        MedicationDoseTask task = requireDoseTask(context.patient().getId(), original.getDoseTaskId());
        if (clientRequestId != null) {
            MedicationCheckinRecord existing = medicationCheckinRecordMapper.selectByClientRequestId(
                    DEFAULT_TENANT_ID, context.patient().getId(), clientRequestId
            );
            if (existing != null) return toCheckinDto(existing);
        }
        if (task.getLockedAfter() != null && task.getLockedAfter().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new BusinessException(
                    ErrorCode.DOSE_TASK_EDIT_WINDOW_EXPIRED.getCode(),
                    ErrorCode.DOSE_TASK_EDIT_WINDOW_EXPIRED.getMessage()
            );
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        MedicationCheckinRecord correction = new MedicationCheckinRecord();
        correction.setDoseTaskId(task.getId());
        correction.setPatientId(context.patient().getId());
        correction.setClientRequestId(clientRequestId);
        correction.setOperatorType(context.operatorType());
        correction.setOperatorId(context.appAccount().getId());
        correction.setResult(normalizeResult(request.getResult()));
        correction.setActualTakenAt(validateActualTakenAt(correction.getResult(), request.getActualTakenAt(), task, now));
        correction.setNote(blankToNull(request.getNote()));
        correction.setSkipReason(blankToNull(request.getSkipReason()));
        correction.setIsCorrection(true);
        correction.setSupersedesRecordId(task.getLastEffectiveCheckinId() != null
                ? task.getLastEffectiveCheckinId() : original.getId());
        correction.setIsEffective(true);
        applyAudit(correction, "app-checkin-correct", now);
        try {
            medicationCheckinRecordMapper.insert(correction);
        } catch (DataIntegrityViolationException ex) {
            MedicationCheckinRecord existing = clientRequestId == null ? null : medicationCheckinRecordMapper.selectByClientRequestIdForUpdate(
                    DEFAULT_TENANT_ID, context.patient().getId(), clientRequestId
            );
            if (existing == null) {
                throw ex;
            }
            return toCheckinDto(existing);
        }
        medicationCheckinRecordMapper.markOthersIneffectiveByDoseTaskId(
                DEFAULT_TENANT_ID, task.getId(), correction.getId(), "app-checkin-correct", now
        );

        task.setStatus(toTaskStatus(correction.getResult()));
        task.setNotificationStatus("ACKED");
        task.setLastEffectiveCheckinId(correction.getId());
        task.setUpdater("app-checkin-correct");
        task.setUpdateTime(now);
        medicationDoseTaskMapper.updateStatus(task);
        return toCheckinDto(correction);
    }

    private MedicationDoseTask requireDoseTask(Long patientId, Long doseTaskId) {
        MedicationDoseTask task = medicationDoseTaskMapper.selectByIdForUpdate(DEFAULT_TENANT_ID, doseTaskId);
        if (task == null || !patientId.equals(task.getPatientId())) {
            throw new BusinessException(ErrorCode.DOSE_TASK_NOT_FOUND.getCode(), ErrorCode.DOSE_TASK_NOT_FOUND.getMessage());
        }
        if ("CANCELLED".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.DOSE_TASK_CANCELLED.getCode(), ErrorCode.DOSE_TASK_CANCELLED.getMessage());
        }
        return task;
    }

    private MedicationPlanDTO toPlanDto(MedicationPlan plan) {
        MedicationPlanDTO dto = new MedicationPlanDTO();
        dto.setId(plan.getId());
        dto.setPatientId(plan.getPatientId());
        dto.setPatientName(plan.getPatientName());
        dto.setPlanName(plan.getPlanName());
        dto.setVersionNo(plan.getVersionNo());
        dto.setStatus(plan.getStatus());
        dto.setSourceType(plan.getSourceType());
        dto.setSourcePrescriptionId(plan.getSourcePrescriptionId());
        dto.setEffectiveFrom(formatDateTime(plan.getEffectiveFrom()));
        dto.setEffectiveTo(formatDateTime(plan.getEffectiveTo()));
        dto.setNotes(plan.getNotes());
        dto.setCreatedByPharmacistId(plan.getCreatedByPharmacistId());
        dto.setConfirmedAt(formatDateTime(plan.getConfirmedAt()));
        dto.setCreatedAt(formatDateTime(plan.getCreateTime()));
        dto.setItems(medicationPlanItemMapper.selectByPlanId(DEFAULT_TENANT_ID, plan.getId()).stream().map(item -> {
            MedicationPlanDTO.ItemDTO itemDto = new MedicationPlanDTO.ItemDTO();
            itemDto.setId(item.getId());
            itemDto.setDrugName(item.getDrugName());
            itemDto.setDrugSpec(item.getDrugSpec());
            itemDto.setDosage(item.getDosage());
            itemDto.setFrequencyCode(item.getFrequencyCode());
            itemDto.setTimePoints(parseTimePoints(item.getTimePointsJson()));
            itemDto.setMealRelation(item.getMealRelation());
            itemDto.setRoute(item.getRoute());
            itemDto.setInstructions(item.getInstructions());
            itemDto.setSortOrder(item.getSortOrder());
            return itemDto;
        }).toList());
        dto.setEvents(medicationPlanEventMapper.selectByPlanId(DEFAULT_TENANT_ID, plan.getId()).stream().map(this::toPlanEventDto).toList());
        return dto;
    }

    private MedicationPlanDTO.EventDTO toPlanEventDto(MedicationPlanEvent event) {
        MedicationPlanDTO.EventDTO dto = new MedicationPlanDTO.EventDTO();
        dto.setId(event.getId());
        dto.setEventType(event.getEventType());
        dto.setContent(event.getContent());
        dto.setEventAt(formatDateTime(event.getCreateTime()));
        dto.setOperatorType(event.getOperatorType());
        dto.setOperatorId(event.getOperatorId());
        return dto;
    }

    private DoseTaskDTO toDoseTaskDto(MedicationDoseTask task) {
        return toDoseTaskDto(task, 0, LocalDateTime.now(ZoneOffset.UTC));
    }

    private DoseTaskDTO toDoseTaskDto(MedicationDoseTask task, int familyBindingCount, LocalDateTime now) {
        DoseTaskDTO dto = new DoseTaskDTO();
        dto.setId(task.getId());
        dto.setPatientId(task.getPatientId());
        dto.setPlanId(task.getPlanId());
        dto.setPlanItemId(task.getPlanItemId());
        dto.setTaskDate(task.getTaskDate() == null ? null : task.getTaskDate().toString());
        dto.setScheduledAt(formatDateTime(task.getScheduledAt()));
        dto.setStatus(task.getStatus());
        dto.setNotificationStatus(task.getNotificationStatus());
        dto.setDelayedGenerated(task.getDelayedGenerated());
        applyReminderStage(dto, task, familyBindingCount, now);
        dto.setLastEffectiveCheckinId(task.getLastEffectiveCheckinId());
        dto.setLockedAfter(formatDateTime(task.getLockedAfter()));
        dto.setDrugName(task.getDrugName());
        dto.setDosage(task.getDosage());
        dto.setFrequencyCode(task.getFrequencyCode());
        dto.setRoute(task.getRoute());
        dto.setMealRelation(task.getMealRelation());
        dto.setInstructions(task.getInstructions());
        return dto;
    }

    private void applyReminderStage(DoseTaskDTO dto, MedicationDoseTask task, int familyBindingCount, LocalDateTime now) {
        long overdueMinutes = calculateOverdueMinutes(task.getScheduledAt(), now);
        boolean pending = "PENDING".equals(task.getStatus());
        boolean familyReminderDue = pending && overdueMinutes >= FAMILY_REMINDER_DELAY_MINUTES && familyBindingCount > 0;
        dto.setOverdueMinutes(overdueMinutes);
        dto.setFamilyBindingCount(familyBindingCount);
        dto.setFamilyReminderDue(familyReminderDue);

        if (HANDLED_TASK_STATUSES.contains(task.getStatus())) {
            dto.setReminderStage("HANDLED");
        } else if (overdueMinutes == 0 && task.getScheduledAt() != null && now.isBefore(task.getScheduledAt())) {
            dto.setReminderStage("UPCOMING");
        } else if (overdueMinutes < FAMILY_REMINDER_DELAY_MINUTES) {
            dto.setReminderStage("DUE");
        } else {
            dto.setReminderStage(familyBindingCount > 0 ? "FAMILY_REMINDER_DUE" : "OVERDUE");
        }
    }

    private long calculateOverdueMinutes(LocalDateTime scheduledAt, LocalDateTime now) {
        if (scheduledAt == null || now.isBefore(scheduledAt)) {
            return 0L;
        }
        return ChronoUnit.MINUTES.between(scheduledAt, now);
    }

    private CheckinRecordDTO toCheckinDto(MedicationCheckinRecord record) {
        CheckinRecordDTO dto = new CheckinRecordDTO();
        dto.setId(record.getId());
        dto.setDoseTaskId(record.getDoseTaskId());
        dto.setPatientId(record.getPatientId());
        dto.setClientRequestId(record.getClientRequestId());
        dto.setOperatorType(record.getOperatorType());
        dto.setOperatorId(record.getOperatorId());
        dto.setResult(record.getResult());
        dto.setActualTakenAt(formatDateTime(record.getActualTakenAt()));
        dto.setNote(record.getNote());
        dto.setSkipReason(record.getSkipReason());
        dto.setCorrection(Boolean.TRUE.equals(record.getIsCorrection()));
        dto.setSupersedesRecordId(record.getSupersedesRecordId());
        dto.setEffective(Boolean.TRUE.equals(record.getIsEffective()));
        dto.setCreatedAt(formatDateTime(record.getCreateTime()));
        return dto;
    }

    private List<String> parseTimePoints(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String normalizeResult(String result) {
        if (!StringUtils.hasText(result)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        String normalized = result.trim().toUpperCase();
        if (!Set.of("TAKEN", "MISSED", "SKIPPED").contains(normalized)) {
            throw new BusinessException(400, "CHECKIN_RESULT_INVALID");
        }
        return normalized;
    }

    private LocalDateTime validateActualTakenAt(String result, String value, MedicationDoseTask task, LocalDateTime now) {
        if (!"TAKEN".equals(result)) return null;
        LocalDateTime actual;
        try {
            actual = StringUtils.hasText(value) ? parseDateTime(value) : now;
        } catch (RuntimeException ex) {
            throw new BusinessException(400, "ACTUAL_TAKEN_AT_INVALID");
        }
        if (actual.isAfter(now) || (task.getScheduledAt() != null && actual.isBefore(task.getScheduledAt().minusHours(24)))) {
            throw new BusinessException(400, "ACTUAL_TAKEN_AT_INVALID");
        }
        return actual;
    }

    private String toTaskStatus(String result) {
        return switch (result) {
            case "MISSED" -> "MISSED";
            case "SKIPPED" -> "SKIPPED";
            default -> "COMPLETED";
        };
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private LocalDate parseRequiredDate(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, "DOSE_TASK_DATE_RANGE_INVALID");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (RuntimeException ex) {
            throw new BusinessException(400, "DOSE_TASK_DATE_RANGE_INVALID");
        }
    }

    private String normalizeClientRequestId(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > 64) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        return normalized;
    }

    private void applyAudit(MedicationCheckinRecord record, String operator, LocalDateTime now) {
        record.setCreator(operator);
        record.setCreateTime(now);
        record.setUpdater(operator);
        record.setUpdateTime(now);
        record.setDeleted(false);
        record.setTenantId(DEFAULT_TENANT_ID);
    }
}
