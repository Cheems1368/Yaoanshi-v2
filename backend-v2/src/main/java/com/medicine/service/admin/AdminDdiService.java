package com.medicine.service.admin;

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
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class AdminDdiService {

    private final DdiRuleMapper ddiRuleMapper;
    private final DrugContraindicationMapper drugContraindicationMapper;
    private final ProfessionalIdentityService professionalIdentityService;

    public AdminDdiService(
            DdiRuleMapper ddiRuleMapper,
            DrugContraindicationMapper drugContraindicationMapper,
            ProfessionalIdentityService professionalIdentityService
    ) {
        this.ddiRuleMapper = ddiRuleMapper;
        this.drugContraindicationMapper = drugContraindicationMapper;
        this.professionalIdentityService = professionalIdentityService;
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
        current.setStatus(defaultStatus(request.getStatus(), current.getStatus()));
        current.setMaintainedByPharmacistId(pharmacist.getId());
        current.setUpdater("pharmacist-" + pharmacist.getId());
        current.setUpdateTime(LocalDateTime.now(ZoneOffset.UTC));
        drugContraindicationMapper.update(current);
        return toContraindicationDto(drugContraindicationMapper.selectById(claims.getTenantId(), id));
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
