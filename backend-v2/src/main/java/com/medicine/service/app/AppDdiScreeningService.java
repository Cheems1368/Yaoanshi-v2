package com.medicine.service.app;

import com.medicine.api.dto.ddi.AdminDdiRiskCardDTO;
import com.medicine.api.dto.ddi.AdminDdiScreenResult;
import com.medicine.api.dto.ddi.AppDdiAlert;
import com.medicine.api.dto.ddi.AppDdiScreenRequest;
import com.medicine.api.dto.ddi.AppDdiScreenResult;
import com.medicine.common.exception.BusinessException;
import com.medicine.domain.ddi.DdiRule;
import com.medicine.domain.ddi.DrugContraindication;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PatientHealthProfile;
import com.medicine.domain.medication.MedicationPlan;
import com.medicine.domain.medication.MedicationPlanItem;
import com.medicine.domain.medication.SelfReportedMedication;
import com.medicine.repository.ddi.DdiRuleMapper;
import com.medicine.repository.ddi.DrugContraindicationMapper;
import com.medicine.repository.identity.PatientHealthProfileMapper;
import com.medicine.repository.medication.MedicationPlanItemMapper;
import com.medicine.repository.medication.MedicationPlanMapper;
import com.medicine.repository.medication.SelfReportedMedicationMapper;
import com.medicine.service.auth.AppPatientService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class AppDdiScreeningService {

    private static final long DEFAULT_TENANT_ID = 1L;

    private static final Map<String, List<String>> DRUG_CLASS_KEYWORDS = Map.ofEntries(
            Map.entry("acei类", List.of("acei", "卡托普利", "依那普利", "赖诺普利", "培哚普利", "贝那普利", "雷米普利", "福辛普利")),
            Map.entry("β受体阻滞剂", List.of("beta blocker", "β受体阻滞剂", "美托洛尔", "比索洛尔", "阿替洛尔", "普萘洛尔", "卡维地洛")),
            Map.entry("尾受体阻滞剂", List.of("美托洛尔", "比索洛尔", "阿替洛尔", "普萘洛尔", "卡维地洛")),
            Map.entry("他汀类", List.of("他汀", "阿托伐他汀", "瑞舒伐他汀", "辛伐他汀", "匹伐他汀", "普伐他汀")),
            Map.entry("ppi", List.of("ppi", "奥美拉唑", "埃索美拉唑", "兰索拉唑", "泮托拉唑", "雷贝拉唑")),
            Map.entry("ppi类", List.of("ppi", "奥美拉唑", "埃索美拉唑", "兰索拉唑", "泮托拉唑", "雷贝拉唑")),
            Map.entry("nsaids", List.of("nsaid", "布洛芬", "双氯芬酸", "塞来昔布", "吲哚美辛", "萘普生")),
            Map.entry("造影剂", List.of("造影剂", "碘海醇", "碘克沙醇", "碘帕醇", "碘普罗胺")),
            Map.entry("钾补充剂", List.of("钾补充剂", "氯化钾", "门冬氨酸钾", "枸橼酸钾", "补钾")),
            Map.entry("保钾利尿剂", List.of("保钾利尿剂", "螺内酯", "阿米洛利", "氨苯蝶啶", "依普利酮"))
    );

    private final AppPatientService appPatientService;
    private final MedicationPlanMapper medicationPlanMapper;
    private final MedicationPlanItemMapper medicationPlanItemMapper;
    private final SelfReportedMedicationMapper selfReportedMedicationMapper;
    private final DdiRuleMapper ddiRuleMapper;
    private final DrugContraindicationMapper drugContraindicationMapper;
    private final PatientHealthProfileMapper patientHealthProfileMapper;

    public AppDdiScreeningService(
            AppPatientService appPatientService,
            MedicationPlanMapper medicationPlanMapper,
            MedicationPlanItemMapper medicationPlanItemMapper,
            SelfReportedMedicationMapper selfReportedMedicationMapper,
            DdiRuleMapper ddiRuleMapper,
            DrugContraindicationMapper drugContraindicationMapper,
            PatientHealthProfileMapper patientHealthProfileMapper
    ) {
        this.appPatientService = appPatientService;
        this.medicationPlanMapper = medicationPlanMapper;
        this.medicationPlanItemMapper = medicationPlanItemMapper;
        this.selfReportedMedicationMapper = selfReportedMedicationMapper;
        this.ddiRuleMapper = ddiRuleMapper;
        this.drugContraindicationMapper = drugContraindicationMapper;
        this.patientHealthProfileMapper = patientHealthProfileMapper;
    }

    public AppDdiScreenResult screenForSubject(String subject, AppDdiScreenRequest request) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        Long tenantId = context.patient().getTenantId() == null ? DEFAULT_TENANT_ID : context.patient().getTenantId();
        return screenPatientMedications(tenantId, context.patient(), request == null ? null : request.getNewDrugNames());
    }

    public AppDdiScreenResult screenPatientMedications(Long patientId, List<String> newDrugNames) {
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setTenantId(DEFAULT_TENANT_ID);
        return screenPatientMedications(DEFAULT_TENANT_ID, patient, newDrugNames);
    }

    private AppDdiScreenResult screenPatientMedications(Long tenantId, Patient patient, List<String> newDrugNames) {
        List<String> normalizedNewDrugs = normalizeDrugNames(newDrugNames);
        if (normalizedNewDrugs.isEmpty()) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        MedicationPlan activePlan = medicationPlanMapper.selectActiveByPatientId(tenantId, patient.getId());
        List<MedicationPlanItem> existingItems = activePlan == null ? List.of() : medicationPlanItemMapper.selectByPlanId(tenantId, activePlan.getId());
        List<SelfReportedMedication> activeSelfReported = selfReportedMedicationMapper.selectActiveByPatientId(tenantId, patient.getId());
        PatientHealthProfile healthProfile = patientHealthProfileMapper.selectByPatientId(tenantId, patient.getId());

        // Build unified background drug name list: ACTIVE plan items + ACTIVE self-reported
        List<String> backgroundDrugNames = new ArrayList<>();
        existingItems.stream().map(MedicationPlanItem::getDrugName).filter(StringUtils::hasText).forEach(backgroundDrugNames::add);
        activeSelfReported.stream().map(SelfReportedMedication::getDrugName).filter(StringUtils::hasText).forEach(backgroundDrugNames::add);

        List<ScreeningHit> hits = collectScreeningHits(
                tenantId,
                patient,
                healthProfile,
                normalizedNewDrugs,
                backgroundDrugNames,
                false
        );

        List<AppDdiAlert> alerts = hits.stream()
                .map(this::toAppAlert)
                .toList();

        AppDdiScreenResult result = new AppDdiScreenResult();
        result.setAlerts(alerts);
        return result;
    }

    private List<ScreeningHit> collectScreeningHits(
            Long tenantId,
            Patient patient,
            PatientHealthProfile healthProfile,
            List<String> subjectDrugNames,
            List<String> backgroundDrugNames,
            boolean includeOtherSubjectDrugs
    ) {
        List<ScreeningHit> hits = new ArrayList<>();
        Set<String> dedupeKeys = new LinkedHashSet<>();
        List<DdiRule> interactionRules = ddiRuleMapper.selectActiveRules(tenantId).stream()
                .filter(rule -> isPatientFacingSeverity(rule.getSeverity()))
                .toList();
        List<DrugContraindication> contraindicationRules = drugContraindicationMapper.selectActiveRules(tenantId).stream()
                .filter(rule -> isPatientFacingSeverity(rule.getSeverity()))
                .toList();

        for (int i = 0; i < subjectDrugNames.size(); i++) {
            String subjectDrug = subjectDrugNames.get(i);
            List<String> effectiveBackgrounds = new ArrayList<>();
            if (includeOtherSubjectDrugs) {
                for (int j = 0; j < subjectDrugNames.size(); j++) {
                    if (i != j) {
                        effectiveBackgrounds.add(subjectDrugNames.get(j));
                    }
                }
            }
            effectiveBackgrounds.addAll(backgroundDrugNames);

            for (String backgroundDrug : effectiveBackgrounds) {
                for (DdiRule rule : interactionRules) {
                    if (!matchesInteraction(rule, backgroundDrug, subjectDrug)) {
                        continue;
                    }
                    ScreeningHit hit = ScreeningHit.forInteraction(rule, backgroundDrug, subjectDrug);
                    if (dedupeKeys.add(hit.dedupeKey())) {
                        hits.add(hit);
                    }
                }
            }
        }

        for (String drug : subjectDrugNames) {
            for (DrugContraindication rule : contraindicationRules) {
                if (!matchesDrug(rule.getDrugName(), drug) || !matchesCondition(rule, patient, healthProfile)) {
                    continue;
                }
                ScreeningHit hit = ScreeningHit.forContraindication(rule, drug);
                if (dedupeKeys.add(hit.dedupeKey())) {
                    hits.add(hit);
                }
            }
        }

        hits.sort(Comparator
                .comparingInt((ScreeningHit hit) -> severityRank(hit.severity()))
                .thenComparing(ScreeningHit::alertType)
                .thenComparing(hit -> normalizeText(hit.sortDrugA()))
                .thenComparing(hit -> normalizeText(hit.sortDrugB())));
        return hits;
    }

    private boolean matchesInteraction(DdiRule rule, String existingDrug, String newDrug) {
        return (matchesDrug(rule.getDrugAName(), existingDrug) && matchesDrug(rule.getDrugBName(), newDrug))
                || (matchesDrug(rule.getDrugBName(), existingDrug) && matchesDrug(rule.getDrugAName(), newDrug));
    }

    private boolean matchesDrug(String ruleDrug, String actualDrug) {
        String normalizedRule = normalizeText(ruleDrug);
        String normalizedActual = normalizeText(actualDrug);
        if (!StringUtils.hasText(normalizedRule) || !StringUtils.hasText(normalizedActual)) {
            return false;
        }
        if (normalizedRule.equals(normalizedActual)
                || normalizedActual.contains(normalizedRule)
                || normalizedRule.contains(normalizedActual)) {
            return true;
        }
        for (String keyword : keywordsFor(normalizedRule)) {
            if (normalizedActual.contains(keyword) || keyword.contains(normalizedActual)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesCondition(DrugContraindication rule, Patient patient, PatientHealthProfile healthProfile) {
        String normalizedType = normalizeText(rule.getConditionType());
        String expected = normalizeText(rule.getConditionValue());
        if (!StringUtils.hasText(expected)) {
            return false;
        }
        return switch (normalizedType) {
            case "disease" -> containsCondition(expected,
                    patient == null ? null : patient.getPrimaryDiseaseName(),
                    patient == null ? null : patient.getMedicalHistoryJson(),
                    healthProfile == null ? null : healthProfile.getLayer1Diagnosis(),
                    healthProfile == null ? null : healthProfile.getLayer2MedicalHistory(),
                    healthProfile == null ? null : healthProfile.getLayer3DetailedSymptoms(),
                    healthProfile == null ? null : healthProfile.getLayer3FamilyHistory(),
                    healthProfile == null ? null : healthProfile.getLayer3SurgeryHistory());
            case "allergy" -> containsCondition(expected,
                    patient == null ? null : patient.getDrugAllergy(),
                    patient == null ? null : patient.getFoodAllergy(),
                    healthProfile == null ? null : healthProfile.getLayer1Allergy());
            case "organ_function" -> containsCondition(expected,
                    patient == null ? null : patient.getMedicalHistoryJson(),
                    healthProfile == null ? null : healthProfile.getLayer2LiverKidney(),
                    healthProfile == null ? null : healthProfile.getLayer2MedicalHistory());
            default -> false;
        };
    }

    private boolean containsCondition(String expected, String... sources) {
        for (String source : sources) {
            String normalizedSource = normalizeText(source);
            if (StringUtils.hasText(normalizedSource) && normalizedSource.contains(expected)) {
                return true;
            }
        }
        return false;
    }

    private List<String> normalizeDrugNames(List<String> names) {
        if (names == null) {
            return List.of();
        }
        return names.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private boolean isPatientFacingSeverity(String severity) {
        String normalized = normalizeText(severity);
        return "high".equals(normalized) || "medium".equals(normalized);
    }

    private int severityRank(String severity) {
        return switch (normalizeText(severity)) {
            case "high" -> 0;
            case "medium" -> 1;
            default -> 2;
        };
    }

    private List<String> keywordsFor(String normalizedRule) {
        List<String> keywords = new ArrayList<>();
        keywords.add(normalizedRule);
        List<String> aliases = DRUG_CLASS_KEYWORDS.get(normalizedRule);
        if (aliases != null) {
            keywords.addAll(aliases.stream().map(this::normalizeText).toList());
        }
        return keywords;
    }

    /**
     * Checks DDI interactions for all drugs within a plan:
     * - Internal: each plan drug vs every other plan drug
     * - External: each plan drug vs the patient's ACTIVE self-reported medications
     * Does NOT include the current ACTIVE medication plan in the background,
     * since this plan is about to replace it.
     */
    public AppDdiScreenResult screenPlanDrugs(Long patientId, List<String> planDrugNames) {
        List<String> normalizedPlanDrugs = normalizeDrugNames(planDrugNames);
        if (normalizedPlanDrugs.isEmpty()) {
            AppDdiScreenResult empty = new AppDdiScreenResult();
            empty.setAlerts(List.of());
            return empty;
        }

        List<SelfReportedMedication> activeSelfReported =
                selfReportedMedicationMapper.selectActiveByPatientId(DEFAULT_TENANT_ID, patientId);
        List<String> selfReportedDrugNames = activeSelfReported.stream()
                .map(SelfReportedMedication::getDrugName)
                .filter(StringUtils::hasText)
                .toList();

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setTenantId(DEFAULT_TENANT_ID);
        PatientHealthProfile healthProfile = patientHealthProfileMapper.selectByPatientId(DEFAULT_TENANT_ID, patientId);

        List<ScreeningHit> hits = collectScreeningHits(
                DEFAULT_TENANT_ID,
                patient,
                healthProfile,
                normalizedPlanDrugs,
                selfReportedDrugNames,
                true
        );

        AppDdiScreenResult result = new AppDdiScreenResult();
        result.setAlerts(hits.stream().map(this::toAppAlert).toList());
        return result;
    }

    public AdminDdiScreenResult screenPlanDrugsDetailed(Long patientId, List<String> planDrugNames) {
        List<String> normalizedPlanDrugs = normalizeDrugNames(planDrugNames);
        AdminDdiScreenResult result = new AdminDdiScreenResult();
        if (normalizedPlanDrugs.isEmpty()) {
            result.setAlerts(List.of());
            return result;
        }

        List<SelfReportedMedication> activeSelfReported =
                selfReportedMedicationMapper.selectActiveByPatientId(DEFAULT_TENANT_ID, patientId);
        List<String> selfReportedDrugNames = activeSelfReported.stream()
                .map(SelfReportedMedication::getDrugName)
                .filter(StringUtils::hasText)
                .toList();

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setTenantId(DEFAULT_TENANT_ID);
        PatientHealthProfile healthProfile = patientHealthProfileMapper.selectByPatientId(DEFAULT_TENANT_ID, patientId);

        List<ScreeningHit> hits = collectScreeningHits(
                DEFAULT_TENANT_ID,
                patient,
                healthProfile,
                normalizedPlanDrugs,
                selfReportedDrugNames,
                true
        );

        result.setAlerts(hits.stream().map(this::toAdminRiskCard).toList());
        return result;
    }

    public AdminDdiRiskCardDTO toInteractionRiskCard(DdiRule rule) {
        AdminDdiRiskCardDTO dto = new AdminDdiRiskCardDTO();
        dto.setCardKey(buildCardKey("INTERACTION", rule.getRuleCode(), rule.getDrugAName(), rule.getDrugBName()));
        dto.setRuleId(rule.getId());
        dto.setRuleCode(rule.getRuleCode());
        dto.setAlertType("INTERACTION");
        dto.setSeverity(rule.getSeverity());
        dto.setDrugA(rule.getDrugAName());
        dto.setDrugB(rule.getDrugBName());
        dto.setMessage(rule.getMessage());
        dto.setRecommendedAction(rule.getRecommendedAction());
        dto.setMechanismDesc(rule.getMechanismDesc());
        dto.setAlternativeRecommendation(rule.getAlternativeRecommendation());
        dto.setReferenceSource(rule.getReferenceSource());
        dto.setPatientSummary(rule.getPatientSummary());
        return dto;
    }

    public AdminDdiRiskCardDTO toContraindicationRiskCard(DrugContraindication rule) {
        AdminDdiRiskCardDTO dto = new AdminDdiRiskCardDTO();
        dto.setCardKey(buildCardKey("CONTRAINDICATION", rule.getRuleCode(), rule.getDrugName(), rule.getConditionValue()));
        dto.setRuleId(rule.getId());
        dto.setRuleCode(rule.getRuleCode());
        dto.setAlertType("CONTRAINDICATION");
        dto.setSeverity(rule.getSeverity());
        dto.setDrugA(rule.getDrugName());
        dto.setDrugB(rule.getConditionValue());
        dto.setConditionType(rule.getConditionType());
        dto.setConditionValue(rule.getConditionValue());
        dto.setMessage(rule.getMessage());
        dto.setRecommendedAction(rule.getRecommendedAction());
        dto.setMechanismDesc(rule.getMechanismDesc());
        dto.setAlternativeRecommendation(rule.getAlternativeRecommendation());
        dto.setReferenceSource(rule.getReferenceSource());
        dto.setPatientSummary(rule.getPatientSummary());
        return dto;
    }

    private AppDdiAlert toAppAlert(ScreeningHit hit) {
        AppDdiAlert alert = new AppDdiAlert();
        alert.setType(hit.alertType());
        alert.setSeverity(hit.severity());
        alert.setDrugA(hit.appDrugA());
        alert.setDrugB(hit.appDrugB());
        alert.setMessage(hit.message());
        alert.setRecommendedAction(hit.recommendedAction());
        alert.setPatientSummary(hit.patientSummary());
        return alert;
    }

    private AdminDdiRiskCardDTO toAdminRiskCard(ScreeningHit hit) {
        if (hit.interactionRule() != null) {
            return toInteractionRiskCard(hit.interactionRule());
        }
        return toContraindicationRiskCard(hit.contraindicationRule());
    }

    private String buildCardKey(String alertType, String ruleCode, String left, String right) {
        return String.join("|",
                normalizeText(alertType),
                normalizeText(ruleCode),
                normalizeText(left),
                normalizeText(right));
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record ScreeningHit(
            String alertType,
            String severity,
            String appDrugA,
            String appDrugB,
            String sortDrugA,
            String sortDrugB,
            String message,
            String recommendedAction,
            String patientSummary,
            DdiRule interactionRule,
            DrugContraindication contraindicationRule,
            String dedupeKey
    ) {
        private static ScreeningHit forInteraction(DdiRule rule, String backgroundDrug, String subjectDrug) {
            return new ScreeningHit(
                    "INTERACTION",
                    rule.getSeverity(),
                    backgroundDrug,
                    subjectDrug,
                    rule.getDrugAName(),
                    rule.getDrugBName(),
                    rule.getMessage(),
                    rule.getRecommendedAction(),
                    StringUtils.hasText(rule.getPatientSummary()) ? rule.getPatientSummary() : rule.getMessage(),
                    rule,
                    null,
                    String.join("|",
                            "interaction",
                            normalizeKey(rule.getRuleCode()),
                            normalizeKey(rule.getDrugAName()),
                            normalizeKey(rule.getDrugBName()))
            );
        }

        private static ScreeningHit forContraindication(DrugContraindication rule, String drugName) {
            return new ScreeningHit(
                    "CONTRAINDICATION",
                    rule.getSeverity(),
                    rule.getConditionValue(),
                    drugName,
                    rule.getDrugName(),
                    rule.getConditionValue(),
                    rule.getMessage(),
                    rule.getRecommendedAction(),
                    StringUtils.hasText(rule.getPatientSummary()) ? rule.getPatientSummary() : rule.getMessage(),
                    null,
                    rule,
                    String.join("|",
                            "contraindication",
                            normalizeKey(rule.getRuleCode()),
                            normalizeKey(rule.getDrugName()),
                            normalizeKey(rule.getConditionType()),
                            normalizeKey(rule.getConditionValue()))
            );
        }

        private static String normalizeKey(String value) {
            return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        }
    }
}
