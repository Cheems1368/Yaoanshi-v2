package com.medicine.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.medication.DoseTaskDTO;
import com.medicine.api.dto.medication.MedicationPlanDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.domain.common.BaseEntity;
import com.medicine.domain.medication.MedicationDoseTask;
import com.medicine.domain.medication.MedicationPlan;
import com.medicine.domain.medication.MedicationPlanEvent;
import com.medicine.domain.medication.MedicationPlanItem;
import com.medicine.repository.medication.MedicationDoseTaskMapper;
import com.medicine.repository.medication.MedicationPlanEventMapper;
import com.medicine.repository.medication.MedicationPlanItemMapper;
import com.medicine.repository.medication.MedicationPlanMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AdminMedicationPlanService {

    private static final Pattern HH_MM_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    private static final long DEFAULT_TENANT_ID = 1L;

    private final MedicationPlanMapper medicationPlanMapper;
    private final MedicationPlanItemMapper medicationPlanItemMapper;
    private final MedicationPlanEventMapper medicationPlanEventMapper;
    private final MedicationDoseTaskMapper medicationDoseTaskMapper;
    private final ProfessionalIdentityService professionalIdentityService;
    private final ObjectMapper objectMapper;

    public AdminMedicationPlanService(
            MedicationPlanMapper medicationPlanMapper,
            MedicationPlanItemMapper medicationPlanItemMapper,
            MedicationPlanEventMapper medicationPlanEventMapper,
            MedicationDoseTaskMapper medicationDoseTaskMapper,
            ProfessionalIdentityService professionalIdentityService,
            ObjectMapper objectMapper
    ) {
        this.medicationPlanMapper = medicationPlanMapper;
        this.medicationPlanItemMapper = medicationPlanItemMapper;
        this.medicationPlanEventMapper = medicationPlanEventMapper;
        this.medicationDoseTaskMapper = medicationDoseTaskMapper;
        this.professionalIdentityService = professionalIdentityService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public MedicationPlanDTO createPlan(JwtClaims claims, MedicationPlanDTO request) {
        Long pharmacistId = professionalIdentityService.getOrCreatePharmacistProfile(claims).getId();
        MedicationPlan latest = medicationPlanMapper.selectLatestByPatientId(DEFAULT_TENANT_ID, request.getPatientId());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        MedicationPlan plan = new MedicationPlan();
        plan.setPatientId(request.getPatientId());
        plan.setPlanName(requireText(request.getPlanName()));
        plan.setVersionNo(latest == null ? 1 : latest.getVersionNo() + 1);
        plan.setStatus(normalizeStatus(request.getStatus(), "DRAFT"));
        plan.setSourceType(StringUtils.hasText(request.getSourceType()) ? request.getSourceType().trim() : "MANUAL");
        plan.setSourcePrescriptionId(request.getSourcePrescriptionId());
        plan.setEffectiveFrom(parseDateTime(request.getEffectiveFrom(), now));
        plan.setEffectiveTo(parseDateTime(request.getEffectiveTo(), null));
        plan.setNotes(blankToNull(request.getNotes()));
        plan.setCreatedByPharmacistId(pharmacistId);
        plan.setConfirmedAt("ACTIVE".equals(plan.getStatus()) ? now : null);
        applyAudit(plan, "admin-med-plan", now);
        medicationPlanMapper.insert(plan);
        replaceItems(plan, request.getItems(), now);
        appendEvent(plan.getId(), "CREATED", request.getNotes(), "PHARMACIST", pharmacistId, now);
        if ("ACTIVE".equals(plan.getStatus())) {
            activateInternal(plan, pharmacistId, now);
        }
        return getPlan(claims, plan.getId());
    }

    @Transactional
    public MedicationPlanDTO updatePlan(JwtClaims claims, Long id, MedicationPlanDTO request) {
        professionalIdentityService.getOrCreatePharmacistProfile(claims);
        MedicationPlan existing = requirePlan(id);
        MedicationPlanDTO copy = new MedicationPlanDTO();
        copy.setPatientId(existing.getPatientId());
        copy.setPlanName(StringUtils.hasText(request.getPlanName()) ? request.getPlanName() : existing.getPlanName());
        copy.setStatus(request.getStatus());
        copy.setSourceType(StringUtils.hasText(request.getSourceType()) ? request.getSourceType() : existing.getSourceType());
        copy.setSourcePrescriptionId(request.getSourcePrescriptionId() == null ? existing.getSourcePrescriptionId() : request.getSourcePrescriptionId());
        copy.setEffectiveFrom(StringUtils.hasText(request.getEffectiveFrom()) ? request.getEffectiveFrom() : formatDateTime(existing.getEffectiveFrom()));
        copy.setEffectiveTo(StringUtils.hasText(request.getEffectiveTo()) ? request.getEffectiveTo() : formatDateTime(existing.getEffectiveTo()));
        copy.setNotes(request.getNotes());
        copy.setItems(request.getItems());
        return createPlan(claims, copy);
    }

    @Transactional
    public MedicationPlanDTO activatePlan(JwtClaims claims, Long id) {
        Long pharmacistId = professionalIdentityService.getOrCreatePharmacistProfile(claims).getId();
        MedicationPlan plan = requirePlan(id);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        activateInternal(plan, pharmacistId, now);
        return getPlan(claims, id);
    }

    public MedicationPlanDTO getPlan(JwtClaims claims, Long id) {
        professionalIdentityService.getOrCreatePharmacistProfile(claims);
        return toPlanDto(requirePlan(id));
    }

    public List<DoseTaskDTO> getPlanTasks(JwtClaims claims, Long id, String dateFrom, String dateTo) {
        professionalIdentityService.getOrCreatePharmacistProfile(claims);
        LocalDate from = StringUtils.hasText(dateFrom) ? LocalDate.parse(dateFrom) : LocalDate.now().minusDays(7);
        LocalDate to = StringUtils.hasText(dateTo) ? LocalDate.parse(dateTo) : LocalDate.now().plusDays(30);
        return medicationDoseTaskMapper.selectByPlanIdAndDateRange(DEFAULT_TENANT_ID, id, from, to).stream().map(this::toDoseTaskDto).toList();
    }

    public String buildAdherenceReport(JwtClaims claims, Long id, String dateFrom, String dateTo) {
        professionalIdentityService.getOrCreatePharmacistProfile(claims);
        LocalDate from = StringUtils.hasText(dateFrom) ? LocalDate.parse(dateFrom) : LocalDate.now().minusDays(30);
        LocalDate to = StringUtils.hasText(dateTo) ? LocalDate.parse(dateTo) : LocalDate.now();
        List<MedicationDoseTask> tasks = medicationDoseTaskMapper.selectByPlanIdAndDateRange(DEFAULT_TENANT_ID, id, from, to);
        long completed = tasks.stream().filter(task -> "COMPLETED".equals(task.getStatus())).count();
        long total = tasks.size();
        double adherence = total == 0 ? 0 : (completed * 100.0d / total);
        return "planId,from,to,totalTasks,completedTasks,adherenceRate\n"
                + id + "," + from + "," + to + "," + total + "," + completed + "," + String.format("%.2f", adherence);
    }

    private void activateInternal(MedicationPlan plan, Long pharmacistId, LocalDateTime now) {
        plan.setStatus("ACTIVE");
        plan.setConfirmedAt(now);
        plan.setUpdater("admin-med-plan");
        plan.setUpdateTime(now);
        medicationPlanMapper.replaceOtherActivePlans(plan);
        medicationPlanMapper.update(plan);
        medicationDoseTaskMapper.softDeleteFutureTasks(DEFAULT_TENANT_ID, plan.getId(), plan.getEffectiveFrom().toLocalDate(), "admin-med-plan", now);
        generateTasks(plan, now);
        appendEvent(plan.getId(), "ACTIVATED", "Plan activated", "PHARMACIST", pharmacistId, now);
    }

    private void generateTasks(MedicationPlan plan, LocalDateTime now) {
        List<MedicationPlanItem> items = medicationPlanItemMapper.selectByPlanId(DEFAULT_TENANT_ID, plan.getId());
        LocalDate start = plan.getEffectiveFrom().toLocalDate();
        LocalDate end = plan.getEffectiveTo() == null ? start.plusDays(29) : plan.getEffectiveTo().toLocalDate();
        if (end.isBefore(start)) {
            end = start;
        }
        if (end.isAfter(start.plusDays(29))) {
            end = start.plusDays(29);
        }

        for (MedicationPlanItem item : items) {
            List<String> timePoints = parseTimePoints(item.getTimePointsJson());
            for (LocalDate cursor = start; !cursor.isAfter(end); cursor = cursor.plusDays(1)) {
                for (String timePoint : timePoints) {
                    LocalTime parsedTime;
                    try {
                        parsedTime = LocalTime.parse(timePoint.trim());
                    } catch (DateTimeParseException e) {
                        throw new BusinessException(ErrorCode.MED_PLAN_INVALID_TIME_POINT.getCode(),
                                "药品「" + item.getDrugName() + "」的服药时间「" + timePoint + "」格式错误，需为 HH:mm");
                    }
                    MedicationDoseTask task = new MedicationDoseTask();
                    task.setPatientId(plan.getPatientId());
                    task.setPlanId(plan.getId());
                    task.setPlanItemId(item.getId());
                    task.setTaskDate(cursor);
                    task.setScheduledAt(LocalDateTime.of(cursor, parsedTime));
                    task.setStatus("PENDING");
                    task.setNotificationStatus("PENDING");
                    task.setLastEffectiveCheckinId(null);
                    task.setLockedAfter(task.getScheduledAt().plusHours(24));
                    task.setDrugName(item.getDrugName());
                    task.setDosage(item.getDosage());
                    task.setFrequencyCode(item.getFrequencyCode());
                    task.setRoute(item.getRoute());
                    task.setMealRelation(item.getMealRelation());
                    task.setInstructions(item.getInstructions());
                    applyAudit(task, "admin-med-plan", now);
                    medicationDoseTaskMapper.insert(task);
                }
            }
        }
    }

    private void replaceItems(MedicationPlan plan, List<MedicationPlanDTO.ItemDTO> items, LocalDateTime now) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        int sortOrder = 0;
        for (MedicationPlanDTO.ItemDTO itemDto : items) {
            validateTimePoints(itemDto.getTimePoints(), itemDto.getDrugName());
            MedicationPlanItem item = new MedicationPlanItem();
            item.setPlanId(plan.getId());
            item.setDrugName(requireText(itemDto.getDrugName()));
            item.setDrugSpec(blankToNull(itemDto.getDrugSpec()));
            item.setDosage(requireText(itemDto.getDosage()));
            item.setFrequencyCode(requireText(itemDto.getFrequencyCode()));
            item.setTimePointsJson(writeTimePoints(itemDto.getTimePoints()));
            item.setMealRelation(blankToNull(itemDto.getMealRelation()));
            item.setRoute(blankToNull(itemDto.getRoute()));
            item.setInstructions(blankToNull(itemDto.getInstructions()));
            item.setSortOrder(sortOrder++);
            applyAudit(item, "admin-med-plan", now);
            medicationPlanItemMapper.insert(item);
        }
    }

    private void validateTimePoints(List<String> timePoints, String drugName) {
        if (timePoints == null || timePoints.isEmpty()) {
            throw new BusinessException(ErrorCode.MED_PLAN_INVALID_TIME_POINT.getCode(),
                    "药品「" + (drugName != null ? drugName.trim() : "") + "」缺少服药时间，请填写 HH:mm 格式，例如 08:00,20:00");
        }
        for (String tp : timePoints) {
            if (tp == null || !HH_MM_PATTERN.matcher(tp.trim()).matches()) {
                throw new BusinessException(ErrorCode.MED_PLAN_INVALID_TIME_POINT.getCode(),
                        "药品「" + (drugName != null ? drugName.trim() : "") + "」的服药时间「" + tp + "」格式错误，需为 HH:mm（24小时制），例如 08:00,20:00");
            }
        }
    }

    private void appendEvent(Long planId, String eventType, String content, String operatorType, Long operatorId, LocalDateTime now) {
        MedicationPlanEvent event = new MedicationPlanEvent();
        event.setPlanId(planId);
        event.setEventType(eventType);
        event.setContent(blankToNull(content));
        event.setOperatorType(operatorType);
        event.setOperatorId(operatorId);
        applyAudit(event, "admin-med-plan", now);
        medicationPlanEventMapper.insert(event);
    }

    private MedicationPlan requirePlan(Long id) {
        MedicationPlan plan = medicationPlanMapper.selectById(DEFAULT_TENANT_ID, id);
        if (plan == null) {
            throw new BusinessException(ErrorCode.MED_PLAN_NOT_FOUND.getCode(), ErrorCode.MED_PLAN_NOT_FOUND.getMessage());
        }
        return plan;
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
        dto.setEvents(medicationPlanEventMapper.selectByPlanId(DEFAULT_TENANT_ID, plan.getId()).stream().map(event -> {
            MedicationPlanDTO.EventDTO eventDto = new MedicationPlanDTO.EventDTO();
            eventDto.setId(event.getId());
            eventDto.setEventType(event.getEventType());
            eventDto.setContent(event.getContent());
            eventDto.setEventAt(formatDateTime(event.getCreateTime()));
            eventDto.setOperatorType(event.getOperatorType());
            eventDto.setOperatorId(event.getOperatorId());
            return eventDto;
        }).toList());
        return dto;
    }

    private DoseTaskDTO toDoseTaskDto(MedicationDoseTask task) {
        DoseTaskDTO dto = new DoseTaskDTO();
        dto.setId(task.getId());
        dto.setPatientId(task.getPatientId());
        dto.setPlanId(task.getPlanId());
        dto.setPlanItemId(task.getPlanItemId());
        dto.setTaskDate(task.getTaskDate() == null ? null : task.getTaskDate().toString());
        dto.setScheduledAt(formatDateTime(task.getScheduledAt()));
        dto.setStatus(task.getStatus());
        dto.setNotificationStatus(task.getNotificationStatus());
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

    private List<String> parseTimePoints(String json) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String writeTimePoints(List<String> timePoints) {
        try {
            return objectMapper.writeValueAsString(timePoints == null ? List.of() : timePoints);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize time points", ex);
        }
    }

    private String requireText(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        return value.trim();
    }

    private String normalizeStatus(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : fallback;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private LocalDateTime parseDateTime(String value, LocalDateTime fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        return OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }

    private void applyAudit(BaseEntity entity, String operator, LocalDateTime now) {
        entity.setCreator(operator);
        entity.setCreateTime(now);
        entity.setUpdater(operator);
        entity.setUpdateTime(now);
        entity.setDeleted(false);
        entity.setTenantId(DEFAULT_TENANT_ID);
    }
}
