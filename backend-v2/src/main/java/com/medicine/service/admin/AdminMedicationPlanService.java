package com.medicine.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.ddi.AdminDdiScreenResult;
import com.medicine.api.dto.medication.DoseTaskDTO;
import com.medicine.api.dto.medication.MedicationPlanDTO;
import com.medicine.api.dto.medication.PlanActivationRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.domain.common.BaseEntity;
import com.medicine.domain.medication.MedicationDoseTask;
import com.medicine.domain.medication.MedicationPlan;
import com.medicine.domain.medication.MedicationPlanEvent;
import com.medicine.domain.medication.MedicationPlanItem;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.repository.identity.PatientMapper;
import com.medicine.repository.medication.MedicationDoseTaskMapper;
import com.medicine.repository.medication.MedicationPlanEventMapper;
import com.medicine.repository.medication.MedicationPlanItemMapper;
import com.medicine.repository.medication.MedicationPlanMapper;
import com.medicine.service.app.AppDdiScreeningService;
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
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Service
public class AdminMedicationPlanService {

    private static final Pattern HH_MM_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    private final MedicationPlanMapper medicationPlanMapper;
    private final MedicationPlanItemMapper medicationPlanItemMapper;
    private final MedicationPlanEventMapper medicationPlanEventMapper;
    private final MedicationDoseTaskMapper medicationDoseTaskMapper;
    private final PatientMapper patientMapper;
    private final AdherenceService adherenceService;
    private final ProfessionalIdentityService professionalIdentityService;
    private final AppDdiScreeningService appDdiScreeningService;
    private final ObjectMapper objectMapper;

    public AdminMedicationPlanService(
            MedicationPlanMapper medicationPlanMapper,
            MedicationPlanItemMapper medicationPlanItemMapper,
            MedicationPlanEventMapper medicationPlanEventMapper,
            MedicationDoseTaskMapper medicationDoseTaskMapper,
            PatientMapper patientMapper,
            AdherenceService adherenceService,
            ProfessionalIdentityService professionalIdentityService,
            AppDdiScreeningService appDdiScreeningService,
            ObjectMapper objectMapper
    ) {
        this.medicationPlanMapper = medicationPlanMapper;
        this.medicationPlanItemMapper = medicationPlanItemMapper;
        this.medicationPlanEventMapper = medicationPlanEventMapper;
        this.medicationDoseTaskMapper = medicationDoseTaskMapper;
        this.patientMapper = patientMapper;
        this.adherenceService = adherenceService;
        this.professionalIdentityService = professionalIdentityService;
        this.appDdiScreeningService = appDdiScreeningService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public MedicationPlanDTO createPlan(JwtClaims claims, MedicationPlanDTO request) {
        if (request == null || request.getPatientId() == null ||
                (StringUtils.hasText(request.getStatus()) && !"DRAFT".equalsIgnoreCase(request.getStatus()))) {
            throw new BusinessException(400, "PLAN_MUST_START_AS_DRAFT");
        }
        Long pharmacistId = requireResponsiblePharmacist(claims, request.getPatientId(), true);
        MedicationPlan latest = medicationPlanMapper.selectLatestByPatientId(claims.getTenantId(), request.getPatientId());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        MedicationPlan plan = new MedicationPlan();
        plan.setPatientId(request.getPatientId());
        plan.setPlanName(requireText(request.getPlanName()));
        plan.setVersionNo(latest == null ? 1 : latest.getVersionNo() + 1);
        plan.setStatus("DRAFT");
        plan.setSourceType(StringUtils.hasText(request.getSourceType()) ? request.getSourceType().trim() : "MANUAL");
        plan.setSourcePrescriptionId(request.getSourcePrescriptionId());
        plan.setEffectiveFrom(parseDateTime(request.getEffectiveFrom(), now));
        plan.setEffectiveTo(parseDateTime(request.getEffectiveTo(), null));
        plan.setNotes(blankToNull(request.getNotes()));
        plan.setCreatedByPharmacistId(pharmacistId);
        plan.setConfirmedAt(null);
        applyAudit(plan, claims.getTenantId(), "admin-med-plan", now);
        medicationPlanMapper.insert(plan);
        replaceItems(plan, request.getItems(), now);
        appendEvent(plan.getId(), claims.getTenantId(), "CREATED", request.getNotes(), "PHARMACIST", pharmacistId, now);
        return getPlan(claims, plan.getId());
    }

    @Transactional
    public MedicationPlanDTO updatePlan(JwtClaims claims, Long id, MedicationPlanDTO request) {
        MedicationPlan existing = requirePlan(claims, id);
        requireResponsiblePharmacist(claims, existing.getPatientId(), true);
        existing = requirePlan(claims, id);
        if (request == null) throw new BusinessException(400, "BAD_REQUEST");
        if ("DRAFT".equals(existing.getStatus())) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            existing.setPlanName(StringUtils.hasText(request.getPlanName()) ? request.getPlanName().trim() : existing.getPlanName());
            existing.setSourceType(StringUtils.hasText(request.getSourceType()) ? request.getSourceType().trim() : existing.getSourceType());
            existing.setSourcePrescriptionId(request.getSourcePrescriptionId() == null ? existing.getSourcePrescriptionId() : request.getSourcePrescriptionId());
            existing.setEffectiveTo(parseDateTime(request.getEffectiveTo(), existing.getEffectiveTo()));
            existing.setNotes(blankToNull(request.getNotes()));
            existing.setUpdater("admin-med-plan");
            existing.setUpdateTime(now);
            medicationPlanMapper.update(existing);
            medicationPlanItemMapper.softDeleteByPlanId(claims.getTenantId(), id, "admin-med-plan", now);
            replaceItems(existing, request.getItems(), now);
            appendEvent(id, claims.getTenantId(), "DRAFT_UPDATED", request.getNotes(), "PHARMACIST",
                    existing.getCreatedByPharmacistId(), now);
            return getPlan(claims, id);
        }
        MedicationPlanDTO copy = new MedicationPlanDTO();
        copy.setPatientId(existing.getPatientId());
        copy.setPlanName(StringUtils.hasText(request.getPlanName()) ? request.getPlanName() : existing.getPlanName());
        copy.setStatus("DRAFT");
        copy.setSourceType(StringUtils.hasText(request.getSourceType()) ? request.getSourceType() : existing.getSourceType());
        copy.setSourcePrescriptionId(request.getSourcePrescriptionId() == null ? existing.getSourcePrescriptionId() : request.getSourcePrescriptionId());
        copy.setEffectiveFrom(StringUtils.hasText(request.getEffectiveFrom()) ? request.getEffectiveFrom() : formatDateTime(existing.getEffectiveFrom()));
        copy.setEffectiveTo(StringUtils.hasText(request.getEffectiveTo()) ? request.getEffectiveTo() : formatDateTime(existing.getEffectiveTo()));
        copy.setNotes(request.getNotes());
        copy.setItems(request.getItems());
        return createPlan(claims, copy);
    }

    @Transactional
    public MedicationPlanDTO activatePlan(JwtClaims claims, Long id, PlanActivationRequest request) {
        MedicationPlan plan = requirePlan(claims, id);
        Long pharmacistId = requireResponsiblePharmacist(claims, plan.getPatientId(), true);
        plan = requirePlan(claims, id);
        if ("ACTIVE".equals(plan.getStatus())) return toPlanDto(plan);
        if (!"DRAFT".equals(plan.getStatus())) throw new BusinessException(409, "PLAN_NOT_DRAFT");
        if (plan.getEffectiveTo() != null && plan.getEffectiveTo().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new BusinessException(409, "PLAN_END_BEFORE_ACTIVATION");
        }
        AdminDdiScreenResult screening = ddiCheckForPlan(claims, id);
        Set<String> requiredKeys = screening.getAlerts().stream().map(alert -> alert.getCardKey()).collect(Collectors.toSet());
        Set<String> confirmedKeys = request == null || request.getAcknowledgedRiskKeys() == null ? Set.of()
                : Set.copyOf(request.getAcknowledgedRiskKeys());
        if (!requiredKeys.equals(confirmedKeys)) throw new BusinessException(409, "DDI_CONFIRM_REQUIRED");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<DoseTaskDTO> conflicts = findActivationConflicts(plan, now);
        if (!conflicts.isEmpty()) applyFirstExecutions(plan, request, now, pharmacistId);
        if (!requiredKeys.isEmpty()) {
            appendEvent(plan.getId(), plan.getTenantId(), "DDI_CONFIRMED", writeTimePoints(requiredKeys.stream().sorted().toList()),
                    "PHARMACIST", pharmacistId, now);
        }
        activateInternal(plan, pharmacistId, now);
        return getPlan(claims, id);
    }

    @Transactional
    public MedicationPlanDTO stopPlan(JwtClaims claims, Long id) {
        MedicationPlan plan = requirePlan(claims, id);
        Long pharmacistId = requireResponsiblePharmacist(claims, plan.getPatientId(), true);
        plan = requirePlan(claims, id);
        if ("STOPPED".equals(plan.getStatus())) return toPlanDto(plan);
        if (!"ACTIVE".equals(plan.getStatus())) throw new BusinessException(409, "PLAN_NOT_ACTIVE");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        plan.setStatus("STOPPED");
        plan.setUpdateTime(now);
        plan.setUpdater("admin-med-plan");
        medicationPlanMapper.update(plan);
        medicationDoseTaskMapper.cancelFutureTasksForPlan(plan.getTenantId(), plan.getPatientId(), plan.getId(), now, "admin-med-plan");
        appendEvent(plan.getId(), plan.getTenantId(), "STOPPED", "Plan stopped", "PHARMACIST", pharmacistId, now);
        return toPlanDto(plan);
    }

    public List<DoseTaskDTO> getActivationConflicts(JwtClaims claims, Long id) {
        MedicationPlan plan = requirePlan(claims, id);
        requireResponsiblePharmacist(claims, plan.getPatientId(), false);
        return findActivationConflicts(plan, LocalDateTime.now(ZoneOffset.UTC));
    }

    private List<DoseTaskDTO> findActivationConflicts(MedicationPlan plan, LocalDateTime now) {
        MedicationPlan old = medicationPlanMapper.selectActiveByPatientId(plan.getTenantId(), plan.getPatientId());
        if (old == null || old.getId().equals(plan.getId())) return List.of();
        return medicationDoseTaskMapper.selectFutureTakenTasks(plan.getTenantId(), plan.getPatientId(), old.getId(), now)
                .stream().map(this::toDoseTaskDto).toList();
    }

    private void applyFirstExecutions(MedicationPlan plan, PlanActivationRequest request, LocalDateTime now, Long pharmacistId) {
        List<MedicationPlanItem> items = medicationPlanItemMapper.selectByPlanId(plan.getTenantId(), plan.getId());
        Map<Long, String> selections = request == null ? null : request.getFirstExecutionAtByItemId();
        if (selections == null || selections.size() != items.size() || !StringUtils.hasText(request.getConflictReason())) {
            throw new BusinessException(409, "PLAN_FIRST_EXECUTION_REQUIRED");
        }
        for (MedicationPlanItem item : items) {
            String value = selections.get(item.getId());
            LocalDateTime selected;
            try {
                selected = parseDateTime(value, null);
            } catch (RuntimeException ex) {
                throw new BusinessException(400, "PLAN_FIRST_EXECUTION_INVALID");
            }
            if (selected == null || !selected.isAfter(now) ||
                    (plan.getEffectiveTo() != null && selected.isAfter(plan.getEffectiveTo()))) {
                throw new BusinessException(400, "PLAN_FIRST_EXECUTION_INVALID");
            }
            String localTimePoint = selected.atOffset(ZoneOffset.UTC).atZoneSameInstant(BUSINESS_ZONE).toLocalTime().toString();
            if (!parseTimePoints(item.getTimePointsJson()).contains(localTimePoint)) {
                throw new BusinessException(400, "PLAN_FIRST_EXECUTION_INVALID");
            }
            item.setFirstExecutionAt(selected);
            item.setUpdater("admin-med-plan");
            item.setUpdateTime(now);
            medicationPlanItemMapper.updateFirstExecution(item);
        }
        appendEvent(plan.getId(), plan.getTenantId(), "FIRST_EXECUTION_SELECTED", request.getConflictReason().trim(),
                "PHARMACIST", pharmacistId, now);
    }

    @Transactional
    public void maintainPatientTasks(Long tenantId, Long patientId) {
        if (patientMapper.lockByIdAndTenant(tenantId, patientId) == null) return;
        MedicationPlan plan = medicationPlanMapper.selectActiveByPatientId(tenantId, patientId);
        if (plan != null) generateTasks(plan, LocalDateTime.now(ZoneOffset.UTC));
    }

    public AdminDdiScreenResult ddiCheckForPlan(JwtClaims claims, Long id) {
        MedicationPlan plan = requirePlan(claims, id);
        requireResponsiblePharmacist(claims, plan.getPatientId(), false);
        List<MedicationPlanItem> items = medicationPlanItemMapper.selectByPlanId(claims.getTenantId(), plan.getId());
        List<String> drugNames = items.stream()
                .map(MedicationPlanItem::getDrugName)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (drugNames.isEmpty()) {
            AdminDdiScreenResult empty = new AdminDdiScreenResult();
            empty.setAlerts(List.of());
            return empty;
        }
        return appDdiScreeningService.screenPlanDrugsDetailed(claims.getTenantId(), plan.getPatientId(), drugNames);
    }

    public MedicationPlanDTO getPlan(JwtClaims claims, Long id) {
        MedicationPlan plan = requirePlan(claims, id);
        requireResponsiblePharmacist(claims, plan.getPatientId(), false);
        return toPlanDto(plan);
    }

    public List<DoseTaskDTO> getPlanTasks(JwtClaims claims, Long id, String dateFrom, String dateTo) {
        MedicationPlan plan = requirePlan(claims, id);
        requireResponsiblePharmacist(claims, plan.getPatientId(), false);
        LocalDate from = StringUtils.hasText(dateFrom) ? LocalDate.parse(dateFrom) : LocalDate.now(BUSINESS_ZONE).minusDays(7);
        LocalDate to = StringUtils.hasText(dateTo) ? LocalDate.parse(dateTo) : LocalDate.now(BUSINESS_ZONE).plusDays(30);
        return medicationDoseTaskMapper.selectByPlanIdAndDateRange(claims.getTenantId(), id, from, to).stream().map(this::toDoseTaskDto).toList();
    }

    public String buildAdherenceReport(JwtClaims claims, Long id, String dateFrom, String dateTo) {
        MedicationPlan plan = requirePlan(claims, id);
        requireResponsiblePharmacist(claims, plan.getPatientId(), false);
        LocalDate from = StringUtils.hasText(dateFrom) ? LocalDate.parse(dateFrom) : LocalDate.now(BUSINESS_ZONE).minusDays(29);
        LocalDate to = StringUtils.hasText(dateTo) ? LocalDate.parse(dateTo) : LocalDate.now(BUSINESS_ZONE);
        AdherenceService.Summary summary = adherenceService.summarizeRange(claims.getTenantId(), plan.getPatientId(), id, from, to,
                LocalDateTime.now(ZoneOffset.UTC));
        String legacyCompletionRate = summary.dueTasks() == 0 ? "" :
                String.format(java.util.Locale.ROOT, "%.2f", summary.completedTasks() * 100.0 / summary.dueTasks());
        String onTimeRate = summary.selfReportedOnTimeRate() == null ? "" :
                String.format(java.util.Locale.ROOT, "%.2f", summary.selfReportedOnTimeRate());
        return "planId,from,to,totalTasks,completedTasks,adherenceRate,asOf,onTimeTakenTasks,earlyOver60Minutes,selfReportedOnTimeRate\n"
                + id + "," + from + "," + to + "," + summary.dueTasks() + "," + summary.completedTasks() + ","
                + legacyCompletionRate + "," + summary.asOf().toInstant(ZoneOffset.UTC) + "," + summary.onTimeTakenTasks()
                + "," + summary.earlyOver60Minutes() + "," + onTimeRate;
    }

    private void activateInternal(MedicationPlan plan, Long pharmacistId, LocalDateTime now) {
        plan.setStatus("ACTIVE");
        plan.setEffectiveFrom(now);
        plan.setConfirmedAt(now);
        plan.setUpdater("admin-med-plan");
        plan.setUpdateTime(now);
        medicationPlanMapper.replaceOtherActivePlans(plan);
        medicationPlanMapper.update(plan);
        medicationDoseTaskMapper.cancelFutureTasksForOtherPlans(plan.getTenantId(), plan.getPatientId(), plan.getId(), now, "admin-med-plan");
        generateTasks(plan, now);
        appendEvent(plan.getId(), plan.getTenantId(), "ACTIVATED", "Plan activated", "PHARMACIST", pharmacistId, now);
    }

    private void generateTasks(MedicationPlan plan, LocalDateTime now) {
        List<MedicationPlanItem> items = medicationPlanItemMapper.selectByPlanId(plan.getTenantId(), plan.getId());
        LocalDate start = plan.getEffectiveFrom().atOffset(ZoneOffset.UTC).atZoneSameInstant(BUSINESS_ZONE).toLocalDate();
        LocalDate horizon = now.atOffset(ZoneOffset.UTC).atZoneSameInstant(BUSINESS_ZONE).toLocalDate().plusDays(29);
        LocalDate end = plan.getEffectiveTo() == null ? horizon : plan.getEffectiveTo().atOffset(ZoneOffset.UTC).atZoneSameInstant(BUSINESS_ZONE).toLocalDate();
        if (end.isBefore(start)) {
            end = start;
        }
        if (end.isAfter(horizon)) {
            end = horizon;
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
                    task.setScheduledAt(LocalDateTime.of(cursor, parsedTime).atZone(BUSINESS_ZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
                    if (task.getScheduledAt().isBefore(plan.getEffectiveFrom()) ||
                            (plan.getEffectiveTo() != null && task.getScheduledAt().isAfter(plan.getEffectiveTo())) ||
                            (item.getFirstExecutionAt() != null && task.getScheduledAt().isBefore(item.getFirstExecutionAt()))) continue;
                    task.setStatus("PENDING");
                    boolean delayed = !task.getScheduledAt().isAfter(now);
                    task.setDelayedGenerated(delayed);
                    task.setNotificationStatus(delayed ? "NOT_REQUIRED" : "PENDING");
                    task.setLastEffectiveCheckinId(null);
                    task.setLockedAfter(task.getScheduledAt().plusHours(24));
                    task.setDrugName(item.getDrugName());
                    task.setDosage(item.getDosage());
                    task.setFrequencyCode(item.getFrequencyCode());
                    task.setRoute(item.getRoute());
                    task.setMealRelation(item.getMealRelation());
                    task.setInstructions(item.getInstructions());
                    applyAudit(task, plan.getTenantId(), "admin-med-plan", now);
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
            applyAudit(item, plan.getTenantId(), "admin-med-plan", now);
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

    private void appendEvent(Long planId, Long tenantId, String eventType, String content, String operatorType, Long operatorId, LocalDateTime now) {
        MedicationPlanEvent event = new MedicationPlanEvent();
        event.setPlanId(planId);
        event.setEventType(eventType);
        event.setContent(blankToNull(content));
        event.setOperatorType(operatorType);
        event.setOperatorId(operatorId);
        applyAudit(event, tenantId, "admin-med-plan", now);
        medicationPlanEventMapper.insert(event);
    }

    private MedicationPlan requirePlan(JwtClaims claims, Long id) {
        if (claims == null || !claims.isProfessional() || claims.getTenantId() == null) {
            throw new BusinessException(ErrorCode.ADMIN_AUTH_INVALID.getCode(), ErrorCode.ADMIN_AUTH_INVALID.getMessage());
        }
        MedicationPlan plan = medicationPlanMapper.selectById(claims.getTenantId(), id);
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
        dto.setItems(medicationPlanItemMapper.selectByPlanId(plan.getTenantId(), plan.getId()).stream().map(item -> {
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
        dto.setEvents(medicationPlanEventMapper.selectByPlanId(plan.getTenantId(), plan.getId()).stream().map(event -> {
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
        dto.setDelayedGenerated(task.getDelayedGenerated());
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

    private void applyAudit(BaseEntity entity, Long tenantId, String operator, LocalDateTime now) {
        entity.setCreator(operator);
        entity.setCreateTime(now);
        entity.setUpdater(operator);
        entity.setUpdateTime(now);
        entity.setDeleted(false);
        entity.setTenantId(tenantId);
    }

    private Long requireResponsiblePharmacist(JwtClaims claims, Long patientId, boolean lock) {
        if (claims == null || !claims.isProfessional() || claims.getTenantId() == null) {
            throw new BusinessException(ErrorCode.ADMIN_AUTH_INVALID.getCode(), ErrorCode.ADMIN_AUTH_INVALID.getMessage());
        }
        Patient patient = lock ? patientMapper.lockByIdAndTenant(claims.getTenantId(), patientId)
                : patientMapper.selectByIdAndTenant(claims.getTenantId(), patientId);
        if (patient == null) throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND.getCode(), ErrorCode.PATIENT_NOT_FOUND.getMessage());
        if (!lock && claims.getRoles() != null && claims.getRoles().stream().anyMatch(role ->
                role != null && List.of("admin", "tenant_admin", "super_admin").contains(role.toLowerCase()))) return null;
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        if (!pharmacist.getId().equals(patient.getBoundByPharmacistId())) {
            throw new BusinessException(ErrorCode.PATIENT_MANAGEMENT_PERMISSION_DENIED.getCode(),
                    ErrorCode.PATIENT_MANAGEMENT_PERMISSION_DENIED.getMessage());
        }
        return pharmacist.getId();
    }
}
