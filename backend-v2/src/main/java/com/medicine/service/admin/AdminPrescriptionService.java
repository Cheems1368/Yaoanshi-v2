package com.medicine.service.admin;

import com.medicine.api.dto.prescription.DdiScreeningConfirmRequest;
import com.medicine.api.dto.prescription.PrescriptionConfirmRequest;
import com.medicine.api.dto.prescription.PrescriptionDTO;
import com.medicine.api.dto.prescription.PrescriptionRejectRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.common.model.PageResult;
import com.medicine.domain.common.BaseEntity;
import com.medicine.domain.ddi.DdiRule;
import com.medicine.domain.ddi.DdiScreeningLog;
import com.medicine.domain.file.FileAsset;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.domain.prescription.Prescription;
import com.medicine.domain.prescription.PrescriptionAttachment;
import com.medicine.domain.prescription.PrescriptionItem;
import com.medicine.domain.prescription.PrescriptionOcrRecord;
import com.medicine.repository.ddi.DdiRuleMapper;
import com.medicine.repository.ddi.DdiScreeningLogMapper;
import com.medicine.repository.file.FileAssetMapper;
import com.medicine.repository.prescription.PrescriptionAttachmentMapper;
import com.medicine.repository.prescription.PrescriptionItemMapper;
import com.medicine.repository.prescription.PrescriptionMapper;
import com.medicine.repository.prescription.PrescriptionOcrRecordMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminPrescriptionService {

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final PrescriptionAttachmentMapper prescriptionAttachmentMapper;
    private final PrescriptionOcrRecordMapper prescriptionOcrRecordMapper;
    private final FileAssetMapper fileAssetMapper;
    private final DdiRuleMapper ddiRuleMapper;
    private final DdiScreeningLogMapper ddiScreeningLogMapper;
    private final ProfessionalIdentityService professionalIdentityService;

    public AdminPrescriptionService(
            PrescriptionMapper prescriptionMapper,
            PrescriptionItemMapper prescriptionItemMapper,
            PrescriptionAttachmentMapper prescriptionAttachmentMapper,
            PrescriptionOcrRecordMapper prescriptionOcrRecordMapper,
            FileAssetMapper fileAssetMapper,
            DdiRuleMapper ddiRuleMapper,
            DdiScreeningLogMapper ddiScreeningLogMapper,
            ProfessionalIdentityService professionalIdentityService
    ) {
        this.prescriptionMapper = prescriptionMapper;
        this.prescriptionItemMapper = prescriptionItemMapper;
        this.prescriptionAttachmentMapper = prescriptionAttachmentMapper;
        this.prescriptionOcrRecordMapper = prescriptionOcrRecordMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.ddiRuleMapper = ddiRuleMapper;
        this.ddiScreeningLogMapper = ddiScreeningLogMapper;
        this.professionalIdentityService = professionalIdentityService;
    }

    public PageResult<PrescriptionDTO> getPrescriptions(JwtClaims claims, String keyword, String status, int pageNo, int pageSize) {
        professionalIdentityService.getOrCreatePharmacistProfile(claims);
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 50);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;
        List<Prescription> rows = prescriptionMapper.searchForAdmin(claims.getTenantId(), normalize(keyword), normalize(status), offset, normalizedPageSize);
        long total = prescriptionMapper.countForAdmin(claims.getTenantId(), normalize(keyword), normalize(status));
        return new PageResult<>(normalizedPageNo, normalizedPageSize, total, rows.stream().map(row -> toDto(claims.getTenantId(), row, false)).toList());
    }

    public PrescriptionDTO getPrescriptionDetail(JwtClaims claims, Long prescriptionId) {
        professionalIdentityService.getOrCreatePharmacistProfile(claims);
        return toDto(claims.getTenantId(), requirePrescription(claims.getTenantId(), prescriptionId), true);
    }

    public PrescriptionDTO confirmPrescription(JwtClaims claims, Long prescriptionId, PrescriptionConfirmRequest request) {
        PharmacistProfile pharmacistProfile = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        Prescription prescription = requirePrescription(claims.getTenantId(), prescriptionId);
        List<PrescriptionConfirmRequest.ItemInput> items = request == null || request.getItems() == null
                ? List.of()
                : request.getItems().stream().filter(item -> item != null && StringUtils.hasText(item.getDrugName())).toList();
        if (items.isEmpty()) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String operator = "pharmacist-" + pharmacistProfile.getId();
        replaceItems(claims.getTenantId(), prescriptionId, items, operator, now);
        ddiScreeningLogMapper.abortPendingByPrescriptionId(claims.getTenantId(), prescriptionId, operator, now);

        List<DdiRule> matchedHighRiskRules = matchHighRiskRules(claims.getTenantId(), items);
        List<DdiScreeningLog> existingLogs = ddiScreeningLogMapper.selectByPrescriptionId(claims.getTenantId(), prescriptionId);
        List<DdiRule> unconfirmedRules = new ArrayList<>();
        for (DdiRule rule : matchedHighRiskRules) {
            boolean confirmed = existingLogs.stream()
                    .filter(log -> Objects.equals(log.getMatchedRuleId(), rule.getId()))
                    .max(Comparator.comparing(DdiScreeningLog::getId))
                    .map(log -> "CONFIRMED_CONTINUE".equals(log.getConfirmStatus()))
                    .orElse(false);
            if (!confirmed) {
                DdiScreeningLog log = new DdiScreeningLog();
                log.setPatientId(prescription.getPatientId());
                log.setPrescriptionId(prescriptionId);
                log.setMatchedRuleId(rule.getId());
                log.setSeverity(rule.getSeverity());
                log.setConfirmStatus("PENDING_CONFIRM");
                applyAudit(log, now, operator, claims.getTenantId());
                ddiScreeningLogMapper.insert(log);
                unconfirmedRules.add(rule);
            }
        }

        if (!unconfirmedRules.isEmpty()) {
            throw new BusinessException(ErrorCode.DDI_CONFIRM_REQUIRED.getCode(), ErrorCode.DDI_CONFIRM_REQUIRED.getMessage());
        }

        prescription.setStatus("PHARMACIST_CONFIRMED");
        prescription.setReviewedByPharmacistId(pharmacistProfile.getId());
        prescription.setReviewedAt(now);
        prescription.setRejectReason(null);
        prescription.setUpdater(operator);
        prescription.setUpdateTime(now);
        prescriptionMapper.updateReviewResult(prescription);
        return getPrescriptionDetail(claims, prescriptionId);
    }

    public PrescriptionDTO rejectPrescription(JwtClaims claims, Long prescriptionId, PrescriptionRejectRequest request) {
        PharmacistProfile pharmacistProfile = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        Prescription prescription = requirePrescription(claims.getTenantId(), prescriptionId);
        if (request == null || !StringUtils.hasText(request.getReason())) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        prescription.setStatus("REJECTED");
        prescription.setReviewedByPharmacistId(pharmacistProfile.getId());
        prescription.setReviewedAt(now);
        prescription.setRejectReason(request.getReason().trim());
        prescription.setUpdater("pharmacist-" + pharmacistProfile.getId());
        prescription.setUpdateTime(now);
        prescriptionMapper.updateReviewResult(prescription);
        return getPrescriptionDetail(claims, prescriptionId);
    }

    public PrescriptionDTO.DdiScreeningDTO confirmScreening(JwtClaims claims, Long screeningId, DdiScreeningConfirmRequest request) {
        PharmacistProfile pharmacistProfile = professionalIdentityService.getOrCreatePharmacistProfile(claims);
        DdiScreeningLog log = ddiScreeningLogMapper.selectByIdAndTenant(claims.getTenantId(), screeningId);
        if (log == null) {
            throw new BusinessException(ErrorCode.DDI_SCREENING_NOT_FOUND.getCode(), ErrorCode.DDI_SCREENING_NOT_FOUND.getMessage());
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String note = request == null || !StringUtils.hasText(request.getNote()) ? "Reviewed by pharmacist" : request.getNote().trim();
        ddiScreeningLogMapper.confirm(
                claims.getTenantId(),
                screeningId,
                pharmacistProfile.getId(),
                note,
                now,
                "pharmacist-" + pharmacistProfile.getId(),
                now
        );
        DdiScreeningLog refreshed = ddiScreeningLogMapper.selectByIdAndTenant(claims.getTenantId(), screeningId);
        DdiRule rule = ddiRuleMapper.selectById(claims.getTenantId(), refreshed.getMatchedRuleId());
        return toDdiDto(refreshed, rule);
    }

    private Prescription requirePrescription(Long tenantId, Long prescriptionId) {
        Prescription prescription = prescriptionMapper.selectByIdAndTenant(tenantId, prescriptionId);
        if (prescription == null) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND.getCode(), ErrorCode.PRESCRIPTION_NOT_FOUND.getMessage());
        }
        return prescription;
    }

    private void replaceItems(Long tenantId, Long prescriptionId, List<PrescriptionConfirmRequest.ItemInput> items, String operator, LocalDateTime now) {
        prescriptionItemMapper.softDeleteByPrescriptionId(tenantId, prescriptionId, operator, now);
        int sortOrder = 0;
        for (PrescriptionConfirmRequest.ItemInput input : items) {
            PrescriptionItem item = new PrescriptionItem();
            item.setPrescriptionId(prescriptionId);
            item.setDrugName(input.getDrugName().trim());
            item.setDrugSpec(blankToNull(input.getDrugSpec()));
            item.setDosage(blankToNull(input.getDosage()));
            item.setFrequency(blankToNull(input.getFrequency()));
            item.setRoute(blankToNull(input.getRoute()));
            item.setDurationDays(input.getDurationDays());
            item.setNormalizedJson(null);
            item.setSource("EDITED");
            item.setSortOrder(sortOrder++);
            applyAudit(item, now, operator, tenantId);
            prescriptionItemMapper.insert(item);
        }
    }

    private List<DdiRule> matchHighRiskRules(Long tenantId, List<PrescriptionConfirmRequest.ItemInput> items) {
        List<String> drugNames = items.stream()
                .map(PrescriptionConfirmRequest.ItemInput::getDrugName)
                .filter(StringUtils::hasText)
                .map(this::normalizeDrugName)
                .distinct()
                .toList();
        if (drugNames.size() < 2) {
            return List.of();
        }
        return ddiRuleMapper.selectActiveRules(tenantId).stream()
                .filter(rule -> "HIGH".equalsIgnoreCase(rule.getSeverity()))
                .filter(rule -> drugNames.contains(normalizeDrugName(rule.getDrugAName()))
                        && drugNames.contains(normalizeDrugName(rule.getDrugBName())))
                .toList();
    }

    private PrescriptionDTO toDto(Long tenantId, Prescription prescription, boolean includeDetails) {
        PrescriptionDTO dto = new PrescriptionDTO();
        dto.setId(prescription.getId());
        dto.setPatientId(prescription.getPatientId());
        dto.setPatientName(prescription.getPatientName());
        dto.setHospitalPatientId(prescription.getHospitalPatientId());
        dto.setStatus(prescription.getStatus());
        dto.setSourceType(prescription.getSourceType());
        dto.setSubmittedByType(prescription.getSubmittedByType());
        dto.setSubmittedById(prescription.getSubmittedById());
        dto.setReviewedByPharmacistId(prescription.getReviewedByPharmacistId());
        dto.setReviewedAt(formatDateTime(prescription.getReviewedAt()));
        dto.setCreatedAt(formatDateTime(prescription.getCreateTime()));
        dto.setRejectReason(prescription.getRejectReason());

        PrescriptionOcrRecord ocrRecord = prescriptionOcrRecordMapper.selectLatestByPrescriptionId(tenantId, prescription.getId());
        dto.setOcrStatus(ocrRecord == null ? null : ocrRecord.getStatus());
        dto.setOcrConfidence(ocrRecord == null || ocrRecord.getConfidence() == null
                ? null
                : ocrRecord.getConfidence().setScale(2, RoundingMode.HALF_UP).toPlainString());

        if (!includeDetails) {
            dto.setAttachments(List.of());
            dto.setItems(List.of());
            dto.setPendingDdiScreenings(List.of());
            return dto;
        }

        dto.setAttachments(loadAttachments(tenantId, prescription.getId()));
        dto.setItems(loadItems(tenantId, prescription.getId()));
        dto.setPendingDdiScreenings(loadScreenings(tenantId, prescription.getId()));
        return dto;
    }

    private List<PrescriptionDTO.AttachmentDTO> loadAttachments(Long tenantId, Long prescriptionId) {
        List<PrescriptionAttachment> attachments = prescriptionAttachmentMapper.selectByPrescriptionId(tenantId, prescriptionId);
        return attachments.stream().map(attachment -> {
            FileAsset fileAsset = fileAssetMapper.selectByIdAndTenant(tenantId, attachment.getFileAssetId());
            PrescriptionDTO.AttachmentDTO dto = new PrescriptionDTO.AttachmentDTO();
            dto.setId(attachment.getId());
            dto.setFileAssetId(attachment.getFileAssetId());
            dto.setAttachmentType(attachment.getAttachmentType());
            if (fileAsset != null) {
                dto.setOriginalFilename(fileAsset.getOriginalFilename());
                dto.setMimeType(fileAsset.getMimeType());
                dto.setSizeBytes(fileAsset.getSizeBytes());
                dto.setObjectKey(fileAsset.getObjectKey());
                dto.setDownloadUrl(fileAsset.getPublicUrl());
            }
            return dto;
        }).toList();
    }

    private List<PrescriptionDTO.ItemDTO> loadItems(Long tenantId, Long prescriptionId) {
        return prescriptionItemMapper.selectByPrescriptionId(tenantId, prescriptionId).stream().map(item -> {
            PrescriptionDTO.ItemDTO dto = new PrescriptionDTO.ItemDTO();
            dto.setId(item.getId());
            dto.setDrugName(item.getDrugName());
            dto.setDrugSpec(item.getDrugSpec());
            dto.setDosage(item.getDosage());
            dto.setFrequency(item.getFrequency());
            dto.setRoute(item.getRoute());
            dto.setDurationDays(item.getDurationDays());
            dto.setSource(item.getSource());
            return dto;
        }).toList();
    }

    private List<PrescriptionDTO.DdiScreeningDTO> loadScreenings(Long tenantId, Long prescriptionId) {
        Map<Long, DdiRule> ruleMap = ddiRuleMapper.selectActiveRules(tenantId).stream()
                .collect(Collectors.toMap(DdiRule::getId, rule -> rule, (left, right) -> left, LinkedHashMap::new));
        return ddiScreeningLogMapper.selectByPrescriptionId(tenantId, prescriptionId).stream()
                .map(log -> toDdiDto(log, ruleMap.get(log.getMatchedRuleId())))
                .toList();
    }

    private PrescriptionDTO.DdiScreeningDTO toDdiDto(DdiScreeningLog log, DdiRule rule) {
        PrescriptionDTO.DdiScreeningDTO dto = new PrescriptionDTO.DdiScreeningDTO();
        dto.setId(log.getId());
        dto.setMatchedRuleId(log.getMatchedRuleId());
        dto.setSeverity(log.getSeverity());
        dto.setConfirmStatus(log.getConfirmStatus());
        dto.setMessage(rule == null ? null : rule.getMessage());
        dto.setRecommendedAction(rule == null ? null : rule.getRecommendedAction());
        dto.setConfirmationNote(log.getConfirmationNote());
        return dto;
    }

    private void applyAudit(BaseEntity entity, LocalDateTime now, String operator, Long tenantId) {
        entity.setCreator(operator);
        entity.setCreateTime(now);
        entity.setUpdater(operator);
        entity.setUpdateTime(now);
        entity.setDeleted(false);
        entity.setTenantId(tenantId);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeDrugName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
