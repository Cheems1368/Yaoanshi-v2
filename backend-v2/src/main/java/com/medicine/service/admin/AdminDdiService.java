package com.medicine.service.admin;

import com.medicine.api.dto.common.CsvImportResultDTO;
import com.medicine.api.dto.ddi.AdminDdiRuleDTO;
import com.medicine.api.dto.ddi.AdminDdiRuleUpsertRequest;
import com.medicine.api.dto.ddi.DrugContraindicationDTO;
import com.medicine.api.dto.ddi.DrugContraindicationUpsertRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.model.PageResult;
import com.medicine.domain.ddi.DdiRule;
import com.medicine.domain.ddi.DrugContraindication;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.repository.ddi.DdiRuleMapper;
import com.medicine.repository.ddi.DrugContraindicationMapper;
import com.medicine.repository.drug.DrugCatalogMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class AdminDdiService {

    private final DdiRuleMapper ddiRuleMapper;
    private final DrugContraindicationMapper drugContraindicationMapper;
    private final ProfessionalIdentityService professionalIdentityService;
    private final DrugCatalogMapper drugCatalogMapper;
    private final CsvImportSupport csvImportSupport;

    public AdminDdiService(
            DdiRuleMapper ddiRuleMapper,
            DrugContraindicationMapper drugContraindicationMapper,
            ProfessionalIdentityService professionalIdentityService,
            DrugCatalogMapper drugCatalogMapper,
            CsvImportSupport csvImportSupport
    ) {
        this.ddiRuleMapper = ddiRuleMapper;
        this.drugContraindicationMapper = drugContraindicationMapper;
        this.professionalIdentityService = professionalIdentityService;
        this.drugCatalogMapper = drugCatalogMapper;
        this.csvImportSupport = csvImportSupport;
    }

    public PageResult<AdminDdiRuleDTO> listRules(JwtClaims claims, String keyword, String severity, int pageNo, int pageSize) {
        professionalIdentityService.getOrCreatePharmacistProfile(claims);
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 50);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;
        String normalizedKeyword = normalizeFilter(keyword);
        String normalizedSeverity = normalizeFilter(severity);
        return new PageResult<>(
                normalizedPageNo,
                normalizedPageSize,
                ddiRuleMapper.countSearch(claims.getTenantId(), normalizedKeyword, normalizedSeverity),
                ddiRuleMapper.search(claims.getTenantId(), normalizedKeyword, normalizedSeverity, offset, normalizedPageSize)
                        .stream()
                        .map(this::toRuleDto)
                        .toList()
        );
    }

    public AdminDdiRuleDTO createRule(JwtClaims claims, AdminDdiRuleUpsertRequest request) {
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        if (request == null) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        DdiRule rule = new DdiRule();
        rule.setRuleCode(requireText(request.getRuleCode()));
        rule.setDrugAName(requireText(request.getDrugAName()));
        rule.setDrugBName(requireText(request.getDrugBName()));
        rule.setSeverity(requireText(request.getSeverity()).toUpperCase());
        rule.setMessage(requireText(request.getMessage()));
        rule.setRecommendedAction(optionalText(request.getRecommendedAction()));
        rule.setMechanismDesc(optionalText(request.getMechanismDesc()));
        rule.setAlternativeRecommendation(optionalText(request.getAlternativeRecommendation()));
        rule.setReferenceSource(optionalText(request.getReferenceSource()));
        rule.setPatientSummary(optionalText(request.getPatientSummary()));
        rule.setStatus(defaultStatus(request.getStatus(), "ACTIVE"));
        rule.setMaintainedByPharmacistId(pharmacist.getId());
        rule.setCreator("pharmacist-" + pharmacist.getId());
        rule.setCreateTime(now);
        rule.setUpdater("pharmacist-" + pharmacist.getId());
        rule.setUpdateTime(now);
        rule.setDeleted(false);
        rule.setTenantId(claims.getTenantId());
        ddiRuleMapper.insert(rule);
        return toRuleDto(rule);
    }

    public AdminDdiRuleDTO updateRule(JwtClaims claims, Long id, AdminDdiRuleUpsertRequest request) {
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        DdiRule current = ddiRuleMapper.selectById(claims.getTenantId(), id);
        if (current == null) {
            throw new BusinessException(404, "DDI_RULE_NOT_FOUND");
        }
        if (request == null) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        current.setDrugAName(requireText(request.getDrugAName()));
        current.setDrugBName(requireText(request.getDrugBName()));
        current.setSeverity(requireText(request.getSeverity()).toUpperCase());
        current.setMessage(requireText(request.getMessage()));
        current.setRecommendedAction(optionalText(request.getRecommendedAction()));
        current.setMechanismDesc(optionalText(request.getMechanismDesc()));
        current.setAlternativeRecommendation(optionalText(request.getAlternativeRecommendation()));
        current.setReferenceSource(optionalText(request.getReferenceSource()));
        current.setPatientSummary(optionalText(request.getPatientSummary()));
        current.setStatus(defaultStatus(request.getStatus(), current.getStatus()));
        current.setMaintainedByPharmacistId(pharmacist.getId());
        current.setUpdater("pharmacist-" + pharmacist.getId());
        current.setUpdateTime(LocalDateTime.now(ZoneOffset.UTC));
        ddiRuleMapper.update(current);
        return toRuleDto(ddiRuleMapper.selectById(claims.getTenantId(), id));
    }

    public PageResult<DrugContraindicationDTO> listContraindications(
            JwtClaims claims,
            String keyword,
            String severity,
            String conditionType,
            int pageNo,
            int pageSize
    ) {
        professionalIdentityService.getOrCreatePharmacistProfile(claims);
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 50);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;
        String normalizedKeyword = normalizeFilter(keyword);
        String normalizedSeverity = normalizeFilter(severity);
        String normalizedConditionType = normalizeFilter(conditionType);
        return new PageResult<>(
                normalizedPageNo,
                normalizedPageSize,
                drugContraindicationMapper.countSearch(claims.getTenantId(), normalizedKeyword, normalizedSeverity, normalizedConditionType),
                drugContraindicationMapper.search(
                                claims.getTenantId(),
                                normalizedKeyword,
                                normalizedSeverity,
                                normalizedConditionType,
                                offset,
                                normalizedPageSize
                        ).stream()
                        .map(this::toContraindicationDto)
                        .toList()
        );
    }

    public DrugContraindicationDTO createContraindication(JwtClaims claims, DrugContraindicationUpsertRequest request) {
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        if (request == null) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        DrugContraindication rule = new DrugContraindication();
        rule.setRuleCode(requireText(request.getRuleCode()));
        rule.setDrugName(requireText(request.getDrugName()));
        rule.setConditionType(requireText(request.getConditionType()).toUpperCase());
        rule.setConditionValue(requireText(request.getConditionValue()));
        rule.setSeverity(requireText(request.getSeverity()).toUpperCase());
        rule.setMessage(requireText(request.getMessage()));
        rule.setRecommendedAction(optionalText(request.getRecommendedAction()));
        rule.setMechanismDesc(optionalText(request.getMechanismDesc()));
        rule.setAlternativeRecommendation(optionalText(request.getAlternativeRecommendation()));
        rule.setReferenceSource(optionalText(request.getReferenceSource()));
        rule.setPatientSummary(optionalText(request.getPatientSummary()));
        rule.setStatus(defaultStatus(request.getStatus(), "ACTIVE"));
        rule.setMaintainedByPharmacistId(pharmacist.getId());
        rule.setCreator("pharmacist-" + pharmacist.getId());
        rule.setCreateTime(now);
        rule.setUpdater("pharmacist-" + pharmacist.getId());
        rule.setUpdateTime(now);
        rule.setDeleted(false);
        rule.setTenantId(claims.getTenantId());
        drugContraindicationMapper.insert(rule);
        return toContraindicationDto(rule);
    }

    public DrugContraindicationDTO updateContraindication(JwtClaims claims, Long id, DrugContraindicationUpsertRequest request) {
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        DrugContraindication current = drugContraindicationMapper.selectById(claims.getTenantId(), id);
        if (current == null) {
            throw new BusinessException(404, "DDI_CONTRAINDICATION_NOT_FOUND");
        }
        if (request == null) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        current.setDrugName(requireText(request.getDrugName()));
        current.setConditionType(requireText(request.getConditionType()).toUpperCase());
        current.setConditionValue(requireText(request.getConditionValue()));
        current.setSeverity(requireText(request.getSeverity()).toUpperCase());
        current.setMessage(requireText(request.getMessage()));
        current.setRecommendedAction(optionalText(request.getRecommendedAction()));
        current.setMechanismDesc(optionalText(request.getMechanismDesc()));
        current.setAlternativeRecommendation(optionalText(request.getAlternativeRecommendation()));
        current.setReferenceSource(optionalText(request.getReferenceSource()));
        current.setPatientSummary(optionalText(request.getPatientSummary()));
        current.setStatus(defaultStatus(request.getStatus(), current.getStatus()));
        current.setMaintainedByPharmacistId(pharmacist.getId());
        current.setUpdater("pharmacist-" + pharmacist.getId());
        current.setUpdateTime(LocalDateTime.now(ZoneOffset.UTC));
        drugContraindicationMapper.update(current);
        return toContraindicationDto(drugContraindicationMapper.selectById(claims.getTenantId(), id));
    }

    public CsvImportResultDTO importRules(JwtClaims claims, MultipartFile file, boolean dryRun) {
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        List<CSVRecord> records = csvImportSupport.parse(file);
        Set<String> seen = csvImportSupport.newSeenSet();
        List<CsvImportResultDTO.RowResult> rows = new ArrayList<>();
        List<Runnable> mutations = new ArrayList<>();
        for (CSVRecord record : records) {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            String ruleCode = csvImportSupport.required(record, "ruleCode", errors);
            csvImportSupport.markDuplicate(ruleCode, seen, errors);
            String drugAName = csvImportSupport.required(record, "drugAName", errors);
            String drugBName = csvImportSupport.required(record, "drugBName", errors);
            String severity = csvImportSupport.severity(csvImportSupport.required(record, "severity", errors), errors);
            String message = csvImportSupport.required(record, "message", errors);
            warnMissingDrug(claims.getTenantId(), drugAName, warnings);
            warnMissingDrug(claims.getTenantId(), drugBName, warnings);
            DdiRule existing = StringUtils.hasText(ruleCode) ? ddiRuleMapper.selectByRuleCode(claims.getTenantId(), ruleCode) : null;
            String action = existing == null ? "CREATE" : "UPDATE";
            if (errors.isEmpty()) {
                DdiRule rule = existing == null ? new DdiRule() : existing;
                applyRuleImportFields(rule, claims.getTenantId(), pharmacist.getId(), ruleCode, drugAName, drugBName, severity, message, record);
                mutations.add(() -> {
                    if (existing == null) {
                        ddiRuleMapper.insert(rule);
                    } else {
                        ddiRuleMapper.update(rule);
                    }
                });
            }
            rows.add(csvImportSupport.rowResult(record, ruleCode, action, errors, warnings));
        }
        CsvImportResultDTO result = csvImportSupport.result(rows);
        if (!dryRun && result.getErrorCount() == 0) {
            mutations.forEach(Runnable::run);
        }
        return result;
    }

    public CsvImportResultDTO importContraindications(JwtClaims claims, MultipartFile file, boolean dryRun) {
        PharmacistProfile pharmacist = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        List<CSVRecord> records = csvImportSupport.parse(file);
        Set<String> seen = csvImportSupport.newSeenSet();
        List<CsvImportResultDTO.RowResult> rows = new ArrayList<>();
        List<Runnable> mutations = new ArrayList<>();
        for (CSVRecord record : records) {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            String ruleCode = csvImportSupport.required(record, "ruleCode", errors);
            csvImportSupport.markDuplicate(ruleCode, seen, errors);
            String drugName = csvImportSupport.required(record, "drugName", errors);
            String conditionType = csvImportSupport.required(record, "conditionType", errors).toUpperCase();
            String conditionValue = csvImportSupport.required(record, "conditionValue", errors);
            String severity = csvImportSupport.severity(csvImportSupport.required(record, "severity", errors), errors);
            String message = csvImportSupport.required(record, "message", errors);
            warnMissingDrug(claims.getTenantId(), drugName, warnings);
            DrugContraindication existing = StringUtils.hasText(ruleCode) ? drugContraindicationMapper.selectByRuleCode(claims.getTenantId(), ruleCode) : null;
            String action = existing == null ? "CREATE" : "UPDATE";
            if (errors.isEmpty()) {
                DrugContraindication rule = existing == null ? new DrugContraindication() : existing;
                applyContraindicationImportFields(rule, claims.getTenantId(), pharmacist.getId(), ruleCode, drugName, conditionType, conditionValue, severity, message, record);
                mutations.add(() -> {
                    if (existing == null) {
                        drugContraindicationMapper.insert(rule);
                    } else {
                        drugContraindicationMapper.update(rule);
                    }
                });
            }
            rows.add(csvImportSupport.rowResult(record, ruleCode, action, errors, warnings));
        }
        CsvImportResultDTO result = csvImportSupport.result(rows);
        if (!dryRun && result.getErrorCount() == 0) {
            mutations.forEach(Runnable::run);
        }
        return result;
    }

    private void applyRuleImportFields(
            DdiRule rule,
            Long tenantId,
            Long pharmacistId,
            String ruleCode,
            String drugAName,
            String drugBName,
            String severity,
            String message,
            CSVRecord record
    ) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (rule.getId() == null) {
            rule.setRuleCode(ruleCode);
            rule.setCreator("pharmacist-" + pharmacistId);
            rule.setCreateTime(now);
            rule.setDeleted(false);
            rule.setTenantId(tenantId);
        }
        rule.setDrugAName(drugAName);
        rule.setDrugBName(drugBName);
        rule.setSeverity(severity);
        rule.setMessage(message);
        rule.setRecommendedAction(csvImportSupport.value(record, "recommendedAction"));
        rule.setMechanismDesc(csvImportSupport.value(record, "mechanismDesc"));
        rule.setAlternativeRecommendation(csvImportSupport.value(record, "alternativeRecommendation"));
        rule.setReferenceSource(csvImportSupport.value(record, "referenceSource"));
        rule.setPatientSummary(csvImportSupport.value(record, "patientSummary"));
        rule.setStatus(csvImportSupport.status(csvImportSupport.value(record, "status")));
        rule.setMaintainedByPharmacistId(pharmacistId);
        rule.setUpdater("pharmacist-" + pharmacistId);
        rule.setUpdateTime(now);
    }

    private void applyContraindicationImportFields(
            DrugContraindication rule,
            Long tenantId,
            Long pharmacistId,
            String ruleCode,
            String drugName,
            String conditionType,
            String conditionValue,
            String severity,
            String message,
            CSVRecord record
    ) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (rule.getId() == null) {
            rule.setRuleCode(ruleCode);
            rule.setCreator("pharmacist-" + pharmacistId);
            rule.setCreateTime(now);
            rule.setDeleted(false);
            rule.setTenantId(tenantId);
        }
        rule.setDrugName(drugName);
        rule.setConditionType(conditionType);
        rule.setConditionValue(conditionValue);
        rule.setSeverity(severity);
        rule.setMessage(message);
        rule.setRecommendedAction(csvImportSupport.value(record, "recommendedAction"));
        rule.setMechanismDesc(csvImportSupport.value(record, "mechanismDesc"));
        rule.setAlternativeRecommendation(csvImportSupport.value(record, "alternativeRecommendation"));
        rule.setReferenceSource(csvImportSupport.value(record, "referenceSource"));
        rule.setPatientSummary(csvImportSupport.value(record, "patientSummary"));
        rule.setStatus(csvImportSupport.status(csvImportSupport.value(record, "status")));
        rule.setMaintainedByPharmacistId(pharmacistId);
        rule.setUpdater("pharmacist-" + pharmacistId);
        rule.setUpdateTime(now);
    }

    private void warnMissingDrug(Long tenantId, String drugName, List<String> warnings) {
        if (StringUtils.hasText(drugName) && drugCatalogMapper.countByNameCn(tenantId, drugName.trim()) == 0) {
            warnings.add("drug not found in catalog: " + drugName.trim());
        }
    }

    private AdminDdiRuleDTO toRuleDto(DdiRule rule) {
        AdminDdiRuleDTO dto = new AdminDdiRuleDTO();
        dto.setId(rule.getId());
        dto.setRuleCode(rule.getRuleCode());
        dto.setDrugAName(rule.getDrugAName());
        dto.setDrugBName(rule.getDrugBName());
        dto.setSeverity(rule.getSeverity());
        dto.setMessage(rule.getMessage());
        dto.setRecommendedAction(rule.getRecommendedAction());
        dto.setMechanismDesc(rule.getMechanismDesc());
        dto.setAlternativeRecommendation(rule.getAlternativeRecommendation());
        dto.setReferenceSource(rule.getReferenceSource());
        dto.setPatientSummary(rule.getPatientSummary());
        dto.setStatus(rule.getStatus());
        dto.setMaintainedByPharmacistId(rule.getMaintainedByPharmacistId());
        dto.setUpdatedAt(formatDateTime(rule.getUpdateTime()));
        return dto;
    }

    private DrugContraindicationDTO toContraindicationDto(DrugContraindication rule) {
        DrugContraindicationDTO dto = new DrugContraindicationDTO();
        dto.setId(rule.getId());
        dto.setRuleCode(rule.getRuleCode());
        dto.setDrugName(rule.getDrugName());
        dto.setConditionType(rule.getConditionType());
        dto.setConditionValue(rule.getConditionValue());
        dto.setSeverity(rule.getSeverity());
        dto.setMessage(rule.getMessage());
        dto.setRecommendedAction(rule.getRecommendedAction());
        dto.setMechanismDesc(rule.getMechanismDesc());
        dto.setAlternativeRecommendation(rule.getAlternativeRecommendation());
        dto.setReferenceSource(rule.getReferenceSource());
        dto.setPatientSummary(rule.getPatientSummary());
        dto.setStatus(rule.getStatus());
        dto.setMaintainedByPharmacistId(rule.getMaintainedByPharmacistId());
        dto.setUpdatedAt(formatDateTime(rule.getUpdateTime()));
        return dto;
    }

    private String normalizeFilter(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String requireText(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        return value.trim();
    }

    private String optionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String defaultStatus(String incoming, String fallback) {
        return StringUtils.hasText(incoming) ? incoming.trim().toUpperCase() : fallback;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }
}
