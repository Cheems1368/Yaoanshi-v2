package com.medicine.service.education;

import com.medicine.api.dto.common.CsvImportResultDTO;
import com.medicine.api.dto.education.EducationContentCardDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.model.PageResult;
import com.medicine.domain.education.EducationContentCard;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PatientHealthProfile;
import com.medicine.domain.medication.MedicationPlan;
import com.medicine.domain.medication.MedicationPlanItem;
import com.medicine.repository.education.EducationContentCardMapper;
import com.medicine.repository.identity.PatientHealthProfileMapper;
import com.medicine.repository.medication.MedicationPlanItemMapper;
import com.medicine.repository.medication.MedicationPlanMapper;
import com.medicine.service.admin.CsvImportSupport;
import com.medicine.service.auth.AppPatientService;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class EducationService {

    private static final long DEFAULT_TENANT_ID = 1L;

    private final EducationContentCardMapper educationContentCardMapper;
    private final AppPatientService appPatientService;
    private final PatientHealthProfileMapper patientHealthProfileMapper;
    private final MedicationPlanMapper medicationPlanMapper;
    private final MedicationPlanItemMapper medicationPlanItemMapper;
    private final ProfessionalIdentityService professionalIdentityService;
    private final CsvImportSupport csvImportSupport;

    public EducationService(
            EducationContentCardMapper educationContentCardMapper,
            AppPatientService appPatientService,
            PatientHealthProfileMapper patientHealthProfileMapper,
            MedicationPlanMapper medicationPlanMapper,
            MedicationPlanItemMapper medicationPlanItemMapper,
            ProfessionalIdentityService professionalIdentityService,
            CsvImportSupport csvImportSupport
    ) {
        this.educationContentCardMapper = educationContentCardMapper;
        this.appPatientService = appPatientService;
        this.patientHealthProfileMapper = patientHealthProfileMapper;
        this.medicationPlanMapper = medicationPlanMapper;
        this.medicationPlanItemMapper = medicationPlanItemMapper;
        this.professionalIdentityService = professionalIdentityService;
        this.csvImportSupport = csvImportSupport;
    }

    public PageResult<EducationContentCardDTO> adminList(JwtClaims claims, String keyword, String category, String reviewStatus, int pageNo, int pageSize) {
        professionalIdentityService.getOrCreatePharmacistProfile(claims);
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 50);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;
        String normalizedStatus = StringUtils.hasText(reviewStatus) ? reviewStatus.trim().toUpperCase() : null;
        List<EducationContentCardDTO> list = educationContentCardMapper
                .search(claims.getTenantId(), normalize(keyword), normalize(category), normalizedStatus, offset, normalizedPageSize)
                .stream()
                .map(card -> toDto(card, true))
                .toList();
        long total = educationContentCardMapper.countSearch(claims.getTenantId(), normalize(keyword), normalize(category), normalizedStatus);
        return new PageResult<>(normalizedPageNo, normalizedPageSize, total, list);
    }

    public CsvImportResultDTO importCards(JwtClaims claims, MultipartFile file, boolean dryRun) {
        professionalIdentityService.getOrCreatePharmacistProfile(claims);
        List<CSVRecord> records = csvImportSupport.parse(file);
        Set<String> seen = csvImportSupport.newSeenSet();
        List<CsvImportResultDTO.RowResult> rows = new ArrayList<>();
        List<Runnable> mutations = new ArrayList<>();
        for (CSVRecord record : records) {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            String contentCode = csvImportSupport.required(record, "contentCode", errors);
            csvImportSupport.markDuplicate(contentCode, seen, errors);
            String title = csvImportSupport.required(record, "title", errors);
            String body = csvImportSupport.required(record, "body", errors);
            String reviewStatus = normalizeReviewStatus(csvImportSupport.value(record, "reviewStatus"), errors);
            LocalDateTime reviewedAt = parseReviewedAt(csvImportSupport.value(record, "reviewedAt"), errors);
            EducationContentCard existing = StringUtils.hasText(contentCode)
                    ? educationContentCardMapper.selectByContentCode(claims.getTenantId(), contentCode)
                    : null;
            String action = existing == null ? "CREATE" : "UPDATE";
            if (errors.isEmpty()) {
                EducationContentCard card = existing == null ? new EducationContentCard() : existing;
                applyCardImportFields(card, claims.getTenantId(), contentCode, title, body, reviewStatus, reviewedAt, record);
                mutations.add(() -> {
                    if (existing == null) {
                        educationContentCardMapper.insert(card);
                    } else {
                        educationContentCardMapper.update(card);
                    }
                });
            }
            rows.add(csvImportSupport.rowResult(record, contentCode, action, errors, warnings));
        }
        CsvImportResultDTO result = csvImportSupport.result(rows);
        if (!dryRun && result.getErrorCount() == 0) {
            mutations.forEach(Runnable::run);
        }
        return result;
    }

    public List<EducationContentCardDTO> recommendations(String subject, int limit) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        List<String> tags = recommendationTags(context.patient());
        int normalizedLimit = Math.min(Math.max(limit, 1), 20);
        return educationContentCardMapper.selectReviewedRecommendations(DEFAULT_TENANT_ID, tags, normalizedLimit)
                .stream()
                .map(card -> toDto(card, false))
                .toList();
    }

    public PageResult<EducationContentCardDTO> appList(String keyword, String category, int pageNo, int pageSize) {
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 50);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;
        List<EducationContentCardDTO> list = educationContentCardMapper
                .search(DEFAULT_TENANT_ID, normalize(keyword), normalize(category), "REVIEWED", offset, normalizedPageSize)
                .stream()
                .map(card -> toDto(card, false))
                .toList();
        long total = educationContentCardMapper.countSearch(DEFAULT_TENANT_ID, normalize(keyword), normalize(category), "REVIEWED");
        return new PageResult<>(normalizedPageNo, normalizedPageSize, total, list);
    }

    public EducationContentCardDTO appDetail(Long id) {
        EducationContentCard card = educationContentCardMapper.selectById(DEFAULT_TENANT_ID, id);
        if (card == null || !"REVIEWED".equals(card.getReviewStatus())) {
            throw new BusinessException(404, "EDUCATION_CARD_NOT_FOUND");
        }
        return toDto(card, true);
    }

    private List<String> recommendationTags(Patient patient) {
        Set<String> tags = new LinkedHashSet<>();
        addTag(tags, patient.getPrimaryDiseaseName());
        addTag(tags, patient.getPrimaryDiseaseCode());
        if (patient.getAge() != null && patient.getAge() >= 65) {
            tags.add("ELDERLY");
            tags.add("老年");
        }
        PatientHealthProfile healthProfile = patientHealthProfileMapper.selectByPatientId(DEFAULT_TENANT_ID, patient.getId());
        if (healthProfile != null) {
            addTag(tags, healthProfile.getLayer1Diagnosis());
            addTag(tags, healthProfile.getLayer2MedicalHistory());
        }
        MedicationPlan plan = medicationPlanMapper.selectActiveByPatientId(DEFAULT_TENANT_ID, patient.getId());
        if (plan != null) {
            for (MedicationPlanItem item : medicationPlanItemMapper.selectByPlanId(DEFAULT_TENANT_ID, plan.getId())) {
                addTag(tags, item.getDrugName());
            }
        }
        tags.add("GENERAL");
        return new ArrayList<>(tags);
    }

    private void applyCardImportFields(
            EducationContentCard card,
            Long tenantId,
            String contentCode,
            String title,
            String body,
            String reviewStatus,
            LocalDateTime reviewedAt,
            CSVRecord record
    ) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (card.getId() == null) {
            card.setContentCode(contentCode);
            card.setCreator("education-import");
            card.setCreateTime(now);
            card.setDeleted(false);
            card.setTenantId(tenantId);
        }
        card.setTitle(title);
        card.setSummary(csvImportSupport.value(record, "summary"));
        card.setBody(body);
        card.setCategory(csvImportSupport.value(record, "category"));
        card.setCategoryName(csvImportSupport.value(record, "categoryName"));
        card.setDiagnosisTags(normalizeTags(csvImportSupport.value(record, "diagnosisTags")));
        card.setDrugTags(normalizeTags(csvImportSupport.value(record, "drugTags")));
        card.setPopulationTags(normalizeTags(csvImportSupport.value(record, "populationTags")));
        card.setSource(csvImportSupport.value(record, "source"));
        card.setReviewStatus(reviewStatus);
        card.setReviewedBy(csvImportSupport.value(record, "reviewedBy"));
        card.setReviewedAt(reviewedAt);
        card.setUpdater("education-import");
        card.setUpdateTime(now);
    }

    private EducationContentCardDTO toDto(EducationContentCard card, boolean includeBody) {
        EducationContentCardDTO dto = new EducationContentCardDTO();
        dto.setId(card.getId());
        dto.setContentCode(card.getContentCode());
        dto.setTitle(card.getTitle());
        dto.setSummary(card.getSummary());
        dto.setBody(includeBody ? card.getBody() : null);
        dto.setCategory(card.getCategory());
        dto.setCategoryName(card.getCategoryName());
        dto.setDiagnosisTags(splitTags(card.getDiagnosisTags()));
        dto.setDrugTags(splitTags(card.getDrugTags()));
        dto.setPopulationTags(splitTags(card.getPopulationTags()));
        dto.setSource(card.getSource());
        dto.setReviewStatus(card.getReviewStatus());
        dto.setReviewedBy(card.getReviewedBy());
        dto.setReviewedAt(format(card.getReviewedAt()));
        dto.setUpdatedAt(format(card.getUpdateTime()));
        return dto;
    }

    private String normalizeReviewStatus(String value, List<String> errors) {
        String status = StringUtils.hasText(value) ? value.trim().toUpperCase() : "DRAFT";
        if (!Set.of("DRAFT", "REVIEWED", "INACTIVE").contains(status)) {
            errors.add("reviewStatus must be DRAFT, REVIEWED, or INACTIVE");
        }
        return status;
    }

    private LocalDateTime parseReviewedAt(String value, List<String> errors) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.trim()).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } catch (Exception ex) {
            errors.add("reviewedAt must be ISO date-time");
            return null;
        }
    }

    private void addTag(Set<String> tags, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        for (String part : value.split("[|,，、;；\\s]+")) {
            if (StringUtils.hasText(part)) {
                tags.add(part.trim());
            }
        }
    }

    private String normalizeTags(String value) {
        return String.join("|", splitTags(value));
    }

    private List<String> splitTags(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split("\\|"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String format(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }
}
