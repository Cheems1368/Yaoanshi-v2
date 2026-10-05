package com.medicine.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.identity.Abcd2AssessmentDTO;
import com.medicine.api.dto.identity.Abcd2InputDTO;
import com.medicine.api.dto.identity.AdminPatientDetailDTO;
import com.medicine.api.dto.identity.AdminPatientListItemDTO;
import com.medicine.api.dto.identity.AdminPatientUpdateRequest;
import com.medicine.api.dto.identity.FamilyBindingSummaryDTO;
import com.medicine.api.dto.identity.PatientAdherenceSummaryDTO;
import com.medicine.api.dto.identity.PatientReassignRequest;
import com.medicine.api.dto.identity.PharmacistOptionDTO;
import com.medicine.api.dto.assessment.KnowledgeAssessmentDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.common.model.PageResult;
import com.medicine.domain.assessment.Abcd2Assessment;
import com.medicine.domain.assessment.KnowledgeAssessment;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.helporder.HelpOrderEvent;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PatientAdminListRow;
import com.medicine.domain.identity.PatientFamilyBinding;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.domain.medication.MedicationPlan;
import com.medicine.repository.assessment.Abcd2AssessmentMapper;
import com.medicine.repository.assessment.KnowledgeAssessmentMapper;
import com.medicine.repository.assessment.AssessmentReminderMapper;
import com.medicine.repository.assessment.SatisfactionSurveyMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.helporder.HelpOrderEventMapper;
import com.medicine.repository.identity.PharmacistProfileMapper;
import com.medicine.repository.identity.PatientFamilyBindingMapper;
import com.medicine.repository.identity.PatientMapper;
import com.medicine.repository.medication.MedicationPlanMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import com.medicine.service.app.KnowledgeQuestionBank;
import com.medicine.service.notification.NotificationDispatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Service
public class AdminPatientService {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private static final String FIELD_GENDER = "性别";
    private static final String FIELD_AGE = "年龄";
    private static final String FIELD_PRIMARY_DISEASE = "主要疾病";
    private static final String FIELD_DRUG_ALLERGY = "药物过敏史";
    private static final String FIELD_ABCD2 = "ABCD2关键信息";

    private final PatientMapper patientMapper;
    private final PatientFamilyBindingMapper patientFamilyBindingMapper;
    private final PharmacistProfileMapper pharmacistProfileMapper;
    private final Abcd2AssessmentMapper abcd2AssessmentMapper;
    private final MedicationPlanMapper medicationPlanMapper;
    private final KnowledgeAssessmentMapper knowledgeAssessmentMapper;
    private final AssessmentReminderMapper assessmentReminderMapper;
    private final SatisfactionSurveyMapper satisfactionSurveyMapper;
    private final HelpOrderMapper helpOrderMapper;
    private final HelpOrderEventMapper helpOrderEventMapper;
    private final NotificationDispatchService notificationDispatchService;
    private final ProfessionalIdentityService professionalIdentityService;
    private final AdherenceService adherenceService;
    private final ObjectMapper objectMapper;

    public AdminPatientService(
            PatientMapper patientMapper,
            PatientFamilyBindingMapper patientFamilyBindingMapper,
            PharmacistProfileMapper pharmacistProfileMapper,
            Abcd2AssessmentMapper abcd2AssessmentMapper,
            MedicationPlanMapper medicationPlanMapper,
            KnowledgeAssessmentMapper knowledgeAssessmentMapper,
            AssessmentReminderMapper assessmentReminderMapper,
            SatisfactionSurveyMapper satisfactionSurveyMapper,
            HelpOrderMapper helpOrderMapper,
            HelpOrderEventMapper helpOrderEventMapper,
            NotificationDispatchService notificationDispatchService,
            ProfessionalIdentityService professionalIdentityService,
            AdherenceService adherenceService,
            ObjectMapper objectMapper
    ) {
        this.patientMapper = patientMapper;
        this.patientFamilyBindingMapper = patientFamilyBindingMapper;
        this.pharmacistProfileMapper = pharmacistProfileMapper;
        this.abcd2AssessmentMapper = abcd2AssessmentMapper;
        this.medicationPlanMapper = medicationPlanMapper;
        this.knowledgeAssessmentMapper = knowledgeAssessmentMapper;
        this.assessmentReminderMapper = assessmentReminderMapper;
        this.satisfactionSurveyMapper = satisfactionSurveyMapper;
        this.helpOrderMapper = helpOrderMapper;
        this.helpOrderEventMapper = helpOrderEventMapper;
        this.notificationDispatchService = notificationDispatchService;
        this.professionalIdentityService = professionalIdentityService;
        this.adherenceService = adherenceService;
        this.objectMapper = objectMapper;
    }

    public List<PharmacistOptionDTO> getActivePharmacists(JwtClaims claims) {
        requireAdmin(claims);
        return pharmacistProfileMapper.selectActive(claims.getTenantId()).stream()
                .map(profile -> new PharmacistOptionDTO(profile.getId(), profile.getRealName())).toList();
    }

    @Transactional
    public AdminPatientDetailDTO reassignPatient(JwtClaims claims, Long patientId, PatientReassignRequest request) {
        requireAdmin(claims);
        if (request == null || request.getPharmacistId() == null || !StringUtils.hasText(request.getReason()))
            throw new BusinessException(400, "REASSIGN_REASON_REQUIRED");
        Patient patient = patientMapper.lockByIdAndTenant(claims.getTenantId(), patientId);
        if (patient == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND.getCode(), ErrorCode.PATIENT_NOT_FOUND.getMessage());
        }
        PharmacistProfile target = pharmacistProfileMapper.selectById(claims.getTenantId(), request.getPharmacistId());
        if (target == null || !"ACTIVE".equals(target.getServiceStatus())) throw new BusinessException(400, "PHARMACIST_NOT_ACTIVE");
        Long oldPharmacistId = patient.getBoundByPharmacistId();
        if (target.getId().equals(oldPharmacistId)) return buildDetailDto(claims.getTenantId(), patient);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<HelpOrder> orders = helpOrderMapper.selectNonTerminalByPatientId(claims.getTenantId(), patientId);
        patient.setBoundByPharmacistId(target.getId());
        patient.setUpdater("admin-reassign-" + claims.getUserId());
        patient.setUpdateTime(now);
        patientMapper.updateResponsiblePharmacist(patient);
        helpOrderMapper.reassignNonTerminalByPatientId(claims.getTenantId(), patientId, oldPharmacistId, target.getId(), patient.getUpdater(), now);
        for (HelpOrder order : orders) appendReassignedEvent(claims, order, oldPharmacistId, target.getId(), request.getReason(), now);
        return buildDetailDto(claims.getTenantId(), patient);
    }

    @Transactional
    public boolean remindKnowledgeAssessment(JwtClaims claims, Long patientId) {
        Patient patient = patientMapper.lockByIdAndTenant(claims.getTenantId(), patientId);
        if (patient == null) throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND.getCode(), ErrorCode.PATIENT_NOT_FOUND.getMessage());
        if (!isAdmin(claims)) {
            PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
            if (!pharmacist.getId().equals(patient.getBoundByPharmacistId())) {
                throw new BusinessException(ErrorCode.PATIENT_MANAGEMENT_PERMISSION_DENIED.getCode(), ErrorCode.PATIENT_MANAGEMENT_PERMISSION_DENIED.getMessage());
            }
        }
        LocalDateTime firstActivatedAt = medicationPlanMapper.selectFirstActivatedAt(claims.getTenantId(), patientId);
        if (firstActivatedAt == null) throw new BusinessException(409, "KNOWLEDGE_ASSESSMENT_NOT_DUE");
        LocalDate today = LocalDate.now(SHANGHAI);
        LocalDate startDate = firstActivatedAt.atOffset(ZoneOffset.UTC).atZoneSameInstant(SHANGHAI).toLocalDate();
        if (today.isBefore(startDate)) throw new BusinessException(409, "KNOWLEDGE_ASSESSMENT_NOT_DUE");
        String phase = ChronoUnit.DAYS.between(startDate, today) >= 27 ? "WEEK_4" : "DAY_1";
        if (knowledgeAssessmentMapper.selectByPatientPhaseAndVersion(claims.getTenantId(), patientId, phase, KnowledgeQuestionBank.VERSION) != null) {
            throw new BusinessException(409, "KNOWLEDGE_ASSESSMENT_NOT_DUE");
        }
        if (assessmentReminderMapper.selectId(claims.getTenantId(), patientId, phase) != null) return true;
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        assessmentReminderMapper.insert(claims.getTenantId(), patientId, phase, now);
        var log = notificationDispatchService.createAndDispatch(claims.getTenantId(), "assessment-reminder", "PATIENT", patientId,
                "KNOWLEDGE_ASSESSMENT_REMINDER", patientId, Map.of("phase", phase), now);
        assessmentReminderMapper.attachLog(claims.getTenantId(), patientId, phase, log.getId());
        return true;
    }

    public PageResult<AdminPatientListItemDTO> getPatients(JwtClaims claims, String keyword, int pageNo, int pageSize) {
        boolean includeAll = isAdmin(claims);
        PharmacistProfile pharmacistProfile = includeAll ? null : professionalIdentityService.getOrCreatePharmacistProfile(claims);
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 50);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;

        List<PatientAdminListRow> rows = patientMapper.searchForPharmacist(
                claims.getTenantId(),
                pharmacistProfile == null ? null : pharmacistProfile.getId(),
                includeAll,
                normalize(keyword),
                offset,
                normalizedPageSize
        );
        long total = patientMapper.countForPharmacist(claims.getTenantId(), pharmacistProfile == null ? null : pharmacistProfile.getId(), includeAll, normalize(keyword));
        List<AdminPatientListItemDTO> items = rows.stream().map(this::toListItem).toList();
        return new PageResult<>(normalizedPageNo, normalizedPageSize, total, items);
    }

    public AdminPatientDetailDTO getPatientDetail(JwtClaims claims, Long patientId) {
        Patient patient = requirePatient(claims.getTenantId(), patientId);
        if (!isAdmin(claims)) {
            PharmacistProfile pharmacistProfile = professionalIdentityService.getOrCreatePharmacistProfile(claims);
            ensurePatientEditableByPharmacist(patient, pharmacistProfile);
        }
        return buildDetailDto(claims.getTenantId(), patient);
    }

    @Transactional
    public AdminPatientDetailDTO confirmBinding(JwtClaims claims, Long patientId) {
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        Patient patient = patientMapper.lockByIdAndTenant(claims.getTenantId(), patientId);
        if (patient == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND.getCode(), ErrorCode.PATIENT_NOT_FOUND.getMessage());
        }
        ensurePatientEditableByPharmacist(patient, pharmacist);
        if ("BOUND".equals(patient.getBindingStatus())) {
            return buildDetailDto(claims.getTenantId(), patient);
        }
        if (!"PENDING".equals(patient.getBindingStatus()) || patient.getAppAccountId() == null) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        patient.setBindingStatus("BOUND");
        patient.setBoundByPharmacistId(pharmacist.getId());
        patient.setBoundAt(now);
        patient.setUpdater("pharmacist-" + pharmacist.getId());
        patient.setUpdateTime(now);
        patientMapper.updateBinding(patient);
        return buildDetailDto(claims.getTenantId(), patient);
    }

    public AdherenceService.Summary getPatientAdherence(JwtClaims claims, Long patientId, int days) {
        Patient patient = requirePatient(claims.getTenantId(), patientId);
        if (!isAdmin(claims)) {
            PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
            ensurePatientEditableByPharmacist(patient, pharmacist);
        }
        return adherenceService.summarize(claims.getTenantId(), patientId, null, days, LocalDateTime.now(ZoneOffset.UTC));
    }

    public String exportPatientAdherence(JwtClaims claims, Long patientId, int days) {
        AdherenceService.Summary summary = getPatientAdherence(claims, patientId, days);
        return adherenceService.toCsv(patientId, null, summary);
    }

    public AdminPatientDetailDTO updatePatient(JwtClaims claims, Long patientId, AdminPatientUpdateRequest request) {
        PharmacistProfile pharmacistProfile = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        Patient patient = requirePatient(claims.getTenantId(), patientId);
        ensurePatientEditableByPharmacist(patient, pharmacistProfile);

        Map<String, Object> beforeMedicalHistory = parseMedicalHistory(patient.getMedicalHistoryJson());
        Map<String, Object> mergedMedicalHistory = mergeMedicalHistory(beforeMedicalHistory, request.getMedicalHistory(), request.getAbcd2Input());
        Map<String, Object> previousAbcd2Input = extractAbcd2Input(beforeMedicalHistory, patient.getAge());

        patient.setName(textOrExisting(request.getName(), patient.getName()));
        patient.setGender(textOrExisting(request.getGender(), patient.getGender()));
        patient.setBirthDate(parseDateOrExisting(request.getBirthDate(), patient.getBirthDate()));
        patient.setAge(request.getAge() == null ? patient.getAge() : request.getAge());
        patient.setPrimaryDiseaseCode(textOrExisting(request.getPrimaryDiseaseCode(), patient.getPrimaryDiseaseCode()));
        patient.setPrimaryDiseaseName(textOrExisting(request.getPrimaryDiseaseName(), patient.getPrimaryDiseaseName()));
        patient.setDrugAllergy(textOrExisting(request.getDrugAllergy(), patient.getDrugAllergy()));
        patient.setFoodAllergy(textOrExisting(request.getFoodAllergy(), patient.getFoodAllergy()));
        patient.setMedicalHistoryJson(writeJson(mergedMedicalHistory));

        if (patient.getBoundByPharmacistId() == null) {
            patient.setBoundByPharmacistId(pharmacistProfile.getId());
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        patient.setUpdater("pharmacist-" + pharmacistProfile.getId());
        patient.setUpdateTime(now);
        patientMapper.updateForPharmacist(patient);

        Map<String, Object> currentAbcd2Input = extractAbcd2Input(mergedMedicalHistory, patient.getAge());
        if (!previousAbcd2Input.equals(currentAbcd2Input)) {
            saveAbcd2Assessment(claims.getTenantId(), patient, pharmacistProfile, currentAbcd2Input, now);
        }

        return buildDetailDto(claims.getTenantId(), requirePatient(claims.getTenantId(), patientId));
    }

    private AdminPatientListItemDTO toListItem(PatientAdminListRow row) {
        AdminPatientListItemDTO item = new AdminPatientListItemDTO();
        item.setId(row.getId());
        item.setName(row.getName());
        item.setMobileMasked(maskMobile(row.getMobile()));
        item.setHospitalPatientId(row.getHospitalPatientId());
        item.setBindingStatus(row.getBindingStatus());
        item.setPrimaryDiseaseName(blankToFallback(row.getPrimaryDiseaseName(), "未填写"));
        item.setResponsiblePharmacistId(row.getBoundByPharmacistId());
        item.setResponsiblePharmacistName(blankToFallback(row.getResponsiblePharmacistName(), row.getBoundByPharmacistId() == null ? "待认领" : "未命名药师"));
        item.setFamilyBindingCount(row.getFamilyBindingCount() == null ? 0 : row.getFamilyBindingCount());
        List<String> pendingFields = buildPendingFields(
                row.getGender(),
                row.getAge(),
                row.getPrimaryDiseaseName(),
                row.getDrugAllergy(),
                row.getMedicalHistoryJson()
        );
        item.setPendingFields(pendingFields);
        item.setPendingFieldsSummary(pendingFields.isEmpty() ? "已完善" : String.join("、", pendingFields));
        return item;
    }

    private AdminPatientDetailDTO buildDetailDto(Long tenantId, Patient patient) {
        AdminPatientDetailDTO detail = new AdminPatientDetailDTO();
        detail.setId(patient.getId());
        detail.setName(patient.getName());
        detail.setMobileMasked(maskMobile(patient.getMobile()));
        detail.setHospitalPatientId(patient.getHospitalPatientId());
        detail.setBindingStatus(patient.getBindingStatus());
        detail.setBoundAt(formatDateTime(patient.getBoundAt()));
        detail.setResponsiblePharmacistId(patient.getBoundByPharmacistId());
        if (patient.getBoundByPharmacistId() != null) {
            PharmacistProfile pharmacistProfile = pharmacistProfileMapper.selectById(tenantId, patient.getBoundByPharmacistId());
            detail.setResponsiblePharmacistName(pharmacistProfile == null ? "未命名药师" : pharmacistProfile.getRealName());
        } else {
            detail.setResponsiblePharmacistName("待认领");
        }
        detail.setGender(patient.getGender());
        detail.setBirthDate(patient.getBirthDate() == null ? null : patient.getBirthDate().toString());
        detail.setAge(patient.getAge());
        detail.setPrimaryDiseaseCode(patient.getPrimaryDiseaseCode());
        detail.setPrimaryDiseaseName(patient.getPrimaryDiseaseName());
        detail.setDrugAllergy(patient.getDrugAllergy());
        detail.setFoodAllergy(patient.getFoodAllergy());

        Map<String, Object> medicalHistory = parseMedicalHistory(patient.getMedicalHistoryJson());
        detail.setMedicalHistory(medicalHistory);
        detail.setAbcd2Input(toAbcd2InputDto(medicalHistory));
        detail.setPendingFields(buildPendingFields(
                patient.getGender(),
                patient.getAge(),
                patient.getPrimaryDiseaseName(),
                patient.getDrugAllergy(),
                patient.getMedicalHistoryJson()
        ));
        detail.setFamilyBindings(loadFamilyBindings(tenantId, patient.getId()));
        detail.setLatestAbcd2Assessment(toAssessmentDto(abcd2AssessmentMapper.selectLatestByPatientId(tenantId, patient.getId())));
        detail.setAdherenceSummary(buildAdherenceSummary(tenantId, patient.getId()));
        detail.setDay1KnowledgeAssessment(toKnowledgeAssessmentDto(knowledgeAssessmentMapper.selectByPatientPhaseAndVersion(
                tenantId, patient.getId(), "DAY_1", KnowledgeQuestionBank.VERSION)));
        detail.setWeek4KnowledgeAssessment(toKnowledgeAssessmentDto(knowledgeAssessmentMapper.selectByPatientPhaseAndVersion(
                tenantId, patient.getId(), "WEEK_4", KnowledgeQuestionBank.VERSION)));
        LocalDateTime firstActivatedAt = medicationPlanMapper.selectFirstActivatedAt(tenantId, patient.getId());
        LocalDate firstDate = firstActivatedAt == null ? null : firstActivatedAt.atOffset(ZoneOffset.UTC).atZoneSameInstant(SHANGHAI).toLocalDate();
        LocalDate today = LocalDate.now(SHANGHAI);
        boolean day1Due = firstDate != null && !today.isBefore(firstDate);
        boolean week4Due = day1Due && !today.isBefore(firstDate.plusDays(27));
        detail.setDay1AssessmentStatus(detail.getDay1KnowledgeAssessment() != null ? "COMPLETED" : day1Due ? "NOT_SUBMITTED" : "NOT_DUE");
        detail.setWeek4AssessmentStatus(detail.getWeek4KnowledgeAssessment() != null ? "COMPLETED" : week4Due ? "NOT_SUBMITTED" : "NOT_DUE");
        detail.setCurrentKnowledgePhase(week4Due ? "WEEK_4" : day1Due ? "DAY_1" : null);
        detail.setSatisfactionCount(satisfactionSurveyMapper.countByPatientId(tenantId, patient.getId()));
        BigDecimal satisfactionAverage = satisfactionSurveyMapper.averageByPatientId(tenantId, patient.getId());
        detail.setSatisfactionAverage(satisfactionAverage == null ? null : satisfactionAverage.doubleValue());
        MedicationPlan latestPlan = medicationPlanMapper.selectLatestByPatientId(tenantId, patient.getId());
        if (latestPlan != null) {
            detail.setLatestMedicationPlanId(latestPlan.getId());
            detail.setLatestMedicationPlanName(latestPlan.getPlanName());
            detail.setLatestMedicationPlanStatus(latestPlan.getStatus());
            detail.setLatestMedicationPlanVersionNo(latestPlan.getVersionNo());
            detail.setLatestMedicationPlanConfirmedAt(formatDateTime(latestPlan.getConfirmedAt()));
        }
        return detail;
    }

    private KnowledgeAssessmentDTO toKnowledgeAssessmentDto(KnowledgeAssessment assessment) {
        if (assessment == null) return null;
        KnowledgeAssessmentDTO dto = new KnowledgeAssessmentDTO();
        dto.setId(assessment.getId()); dto.setPhase(assessment.getPhase()); dto.setQuestionSetVersion(assessment.getQuestionSetVersion());
        dto.setScore(assessment.getScore().doubleValue()); dto.setMaxScore(assessment.getMaxScore().doubleValue());
        dto.setSubmittedAt(formatDateTime(assessment.getSubmittedAt()));
        return dto;
    }

    private void appendReassignedEvent(JwtClaims claims, HelpOrder order, Long oldId, Long newId, String reason, LocalDateTime now) {
        HelpOrderEvent event = new HelpOrderEvent();
        event.setHelpOrderId(order.getId()); event.setEventType("REASSIGNED"); event.setFromStatus(order.getStatus()); event.setToStatus(order.getStatus());
        event.setOperatorType("ADMIN"); event.setOperatorId(claims.getUserId());
        event.setContent(StringUtils.hasText(reason) ? reason.trim() : "责任药师改派");
        event.setMetadataJson(writeJson(Map.of("oldPharmacistId", oldId == null ? 0L : oldId, "newPharmacistId", newId)));
        event.setCreator("admin-reassign-" + claims.getUserId()); event.setCreateTime(now); event.setUpdater(event.getCreator());
        event.setUpdateTime(now); event.setDeleted(false); event.setTenantId(claims.getTenantId());
        helpOrderEventMapper.insert(event);
    }

    private void requireAdmin(JwtClaims claims) {
        if (!isAdmin(claims)) throw new BusinessException(ErrorCode.PATIENT_MANAGEMENT_PERMISSION_DENIED.getCode(), ErrorCode.PATIENT_MANAGEMENT_PERMISSION_DENIED.getMessage());
    }

    private boolean isAdmin(JwtClaims claims) {
        Set<String> adminRoles = Set.of("admin", "super_admin", "tenant_admin");
        return claims != null && claims.isProfessional() && claims.getRoles() != null
                && claims.getRoles().stream().anyMatch(role -> role != null && adminRoles.contains(role.toLowerCase()));
    }

    private List<FamilyBindingSummaryDTO> loadFamilyBindings(Long tenantId, Long patientId) {
        List<PatientFamilyBinding> bindings = patientFamilyBindingMapper.selectActiveByPatientId(tenantId, patientId);
        return bindings.stream().map(binding -> {
            FamilyBindingSummaryDTO dto = new FamilyBindingSummaryDTO();
            dto.setId(binding.getId());
            dto.setFamilyAccountId(binding.getFamilyAccountId());
            dto.setRelationshipType(binding.getRelationshipType());
            dto.setStatus(binding.getStatus());
            dto.setAuthorizedAt(formatDateTime(binding.getAuthorizedAt()));
            return dto;
        }).toList();
    }

    private PatientAdherenceSummaryDTO buildAdherenceSummary(Long tenantId, Long patientId) {
        LocalDateTime asOf = LocalDateTime.now(ZoneOffset.UTC);
        AdherenceService.Summary seven = adherenceService.summarize(tenantId, patientId, null, 7, asOf);
        AdherenceService.Summary thirty = adherenceService.summarize(tenantId, patientId, null, 30, asOf);
        PatientAdherenceSummaryDTO summary = new PatientAdherenceSummaryDTO();
        summary.setStatus(thirty.dueTasks() == 0 ? "NOT_AVAILABLE" : "AVAILABLE");
        summary.setSevenDayAdherence(seven.selfReportedOnTimeRate());
        summary.setThirtyDayAdherence(thirty.selfReportedOnTimeRate());
        summary.setAsOf(asOf.toInstant(ZoneOffset.UTC).toString());
        summary.setSevenDayDueTasks(seven.dueTasks());
        summary.setSevenDayOnTimeTaken(seven.onTimeTakenTasks());
        summary.setSevenDayEarlyOver60Minutes(seven.earlyOver60Minutes());
        summary.setSevenDayPendingOnTimeWindow(seven.pendingOnTimeWindow());
        summary.setThirtyDayDueTasks(thirty.dueTasks());
        summary.setThirtyDayOnTimeTaken(thirty.onTimeTakenTasks());
        summary.setThirtyDayEarlyOver60Minutes(thirty.earlyOver60Minutes());
        summary.setThirtyDayPendingOnTimeWindow(thirty.pendingOnTimeWindow());
        return summary;
    }

    private Abcd2AssessmentDTO toAssessmentDto(Abcd2Assessment assessment) {
        if (assessment == null) {
            return null;
        }
        Abcd2AssessmentDTO dto = new Abcd2AssessmentDTO();
        dto.setId(assessment.getId());
        dto.setScore(assessment.getScore());
        dto.setRiskLevel(assessment.getRiskLevel());
        dto.setCalculatedAt(formatDateTime(assessment.getCalculatedAt()));
        dto.setRawInput(parseJsonToMap(assessment.getRawInputJson()));
        return dto;
    }

    private void saveAbcd2Assessment(
            Long tenantId,
            Patient patient,
            PharmacistProfile pharmacistProfile,
            Map<String, Object> input,
            LocalDateTime now
    ) {
        Abcd2Assessment assessment = new Abcd2Assessment();
        assessment.setPatientId(patient.getId());
        assessment.setSourceType("PHARMACIST_ENTRY");
        assessment.setSourceRefId(patient.getId());
        assessment.setRawInputJson(writeJson(input));
        assessment.setScore(calculateScore(input));
        assessment.setRiskLevel(determineRiskLevel(input, assessment.getScore()));
        assessment.setCalculatedAt(now);
        assessment.setCalculatedById(pharmacistProfile.getId());
        assessment.setCreator("pharmacist-" + pharmacistProfile.getId());
        assessment.setCreateTime(now);
        assessment.setUpdater("pharmacist-" + pharmacistProfile.getId());
        assessment.setUpdateTime(now);
        assessment.setDeleted(false);
        assessment.setTenantId(tenantId);
        abcd2AssessmentMapper.insert(assessment);
    }

    private int calculateScore(Map<String, Object> input) {
        int score = 0;
        if (Boolean.TRUE.equals(input.get("age60OrOlder"))) {
            score += 1;
        }
        if (Boolean.TRUE.equals(input.get("bloodPressureHigh"))) {
            score += 1;
        }
        String clinicalFeature = stringValue(input.get("clinicalFeature"));
        if ("UNILATERAL_WEAKNESS".equals(clinicalFeature)) {
            score += 2;
        } else if ("SPEECH_IMPAIRMENT".equals(clinicalFeature)) {
            score += 1;
        }
        Integer duration = integerValue(input.get("symptomDurationMinutes"));
        if (duration != null && duration >= 60) {
            score += 2;
        } else if (duration != null && duration >= 10) {
            score += 1;
        }
        if (Boolean.TRUE.equals(input.get("diabetes"))) {
            score += 1;
        }
        return score;
    }

    private String determineRiskLevel(Map<String, Object> input, int score) {
        if (input.values().stream().anyMatch(value -> value == null)) {
            return "INCOMPLETE";
        }
        if (score >= 6) {
            return "HIGH";
        }
        if (score >= 4) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private List<String> buildPendingFields(String gender, Integer age, String primaryDiseaseName, String drugAllergy, String medicalHistoryJson) {
        List<String> pendingFields = new ArrayList<>();
        if (!StringUtils.hasText(gender)) {
            pendingFields.add(FIELD_GENDER);
        }
        if (age == null) {
            pendingFields.add(FIELD_AGE);
        }
        if (!StringUtils.hasText(primaryDiseaseName)) {
            pendingFields.add(FIELD_PRIMARY_DISEASE);
        }
        if (!StringUtils.hasText(drugAllergy)) {
            pendingFields.add(FIELD_DRUG_ALLERGY);
        }
        Map<String, Object> abcd2Input = extractAbcd2Input(parseMedicalHistory(medicalHistoryJson), age);
        if (abcd2Input.values().stream().anyMatch(value -> value == null)) {
            pendingFields.add(FIELD_ABCD2);
        }
        return pendingFields;
    }

    private Map<String, Object> mergeMedicalHistory(
            Map<String, Object> currentMedicalHistory,
            Map<String, Object> updateMedicalHistory,
            Abcd2InputDTO abcd2Input
    ) {
        Map<String, Object> merged = new LinkedHashMap<>(currentMedicalHistory);
        if (updateMedicalHistory != null) {
            merged.putAll(updateMedicalHistory);
        }
        if (abcd2Input != null) {
            Map<String, Object> abcd2 = new LinkedHashMap<>();
            abcd2.put("bloodPressureHigh", abcd2Input.getBloodPressureHigh());
            abcd2.put("clinicalFeature", normalizeClinicalFeature(abcd2Input.getClinicalFeature()));
            abcd2.put("symptomDurationMinutes", abcd2Input.getSymptomDurationMinutes());
            abcd2.put("diabetes", abcd2Input.getDiabetes());
            merged.put("abcd2", abcd2);
        }
        return merged;
    }

    private Abcd2InputDTO toAbcd2InputDto(Map<String, Object> medicalHistory) {
        Map<String, Object> abcd2 = nestedMap(medicalHistory.get("abcd2"));
        Abcd2InputDTO dto = new Abcd2InputDTO();
        dto.setBloodPressureHigh(booleanValue(abcd2.get("bloodPressureHigh")));
        dto.setClinicalFeature(stringValue(abcd2.get("clinicalFeature")));
        dto.setSymptomDurationMinutes(integerValue(abcd2.get("symptomDurationMinutes")));
        dto.setDiabetes(booleanValue(abcd2.get("diabetes")));
        return dto;
    }

    private Map<String, Object> extractAbcd2Input(Map<String, Object> medicalHistory, Integer age) {
        Map<String, Object> abcd2 = nestedMap(medicalHistory.get("abcd2"));
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("age60OrOlder", age == null ? null : age >= 60);
        input.put("bloodPressureHigh", booleanValue(abcd2.get("bloodPressureHigh")));
        input.put("clinicalFeature", normalizeClinicalFeature(stringValue(abcd2.get("clinicalFeature"))));
        input.put("symptomDurationMinutes", integerValue(abcd2.get("symptomDurationMinutes")));
        input.put("diabetes", booleanValue(abcd2.get("diabetes")));
        return input;
    }

    private Patient requirePatient(Long tenantId, Long patientId) {
        Patient patient = patientMapper.selectByIdAndTenant(tenantId, patientId);
        if (patient == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND.getCode(), ErrorCode.PATIENT_NOT_FOUND.getMessage());
        }
        return patient;
    }

    private void ensurePatientEditableByPharmacist(Patient patient, PharmacistProfile pharmacistProfile) {
        if (patient.getBoundByPharmacistId() != null && !patient.getBoundByPharmacistId().equals(pharmacistProfile.getId())) {
            throw new BusinessException(
                    ErrorCode.PATIENT_MANAGEMENT_PERMISSION_DENIED.getCode(),
                    ErrorCode.PATIENT_MANAGEMENT_PERMISSION_DENIED.getMessage()
            );
        }
    }

    private Map<String, Object> parseMedicalHistory(String medicalHistoryJson) {
        return parseJsonToMap(medicalHistoryJson);
    }

    private Map<String, Object> parseJsonToMap(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    private String writeJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data == null ? Map.of() : data);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize medical history", ex);
        }
    }

    private Map<String, Object> nestedMap(Object value) {
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> nested = new LinkedHashMap<>();
            rawMap.forEach((key, entryValue) -> nested.put(String.valueOf(key), entryValue));
            return nested;
        }
        return new LinkedHashMap<>();
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }

    private String maskMobile(String mobile) {
        if (!StringUtils.hasText(mobile) || mobile.length() < 7) {
            return blankToFallback(mobile, "--");
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }

    private String normalize(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return keyword.trim();
    }

    private String textOrExisting(String incoming, String existing) {
        return StringUtils.hasText(incoming) ? incoming.trim() : existing;
    }

    private LocalDate parseDateOrExisting(String value, LocalDate existing) {
        if (!StringUtils.hasText(value)) {
            return existing;
        }
        return LocalDate.parse(value);
    }

    private String blankToFallback(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Integer integerValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Boolean booleanValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private String normalizeClinicalFeature(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toUpperCase();
    }
}
