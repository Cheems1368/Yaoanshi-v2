package com.medicine.service.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.prescription.PrescriptionDTO;
import com.medicine.api.dto.prescription.PrescriptionUploadRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.common.model.PageResult;
import com.medicine.config.ObjectStorageProperties;
import com.medicine.domain.common.BaseEntity;
import com.medicine.domain.file.FileAsset;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.prescription.Prescription;
import com.medicine.domain.prescription.PrescriptionAttachment;
import com.medicine.domain.prescription.PrescriptionItem;
import com.medicine.domain.prescription.PrescriptionOcrRecord;
import com.medicine.repository.file.FileAssetMapper;
import com.medicine.repository.identity.AppAccountMapper;
import com.medicine.repository.identity.PatientMapper;
import com.medicine.repository.prescription.PrescriptionAttachmentMapper;
import com.medicine.repository.prescription.PrescriptionItemMapper;
import com.medicine.repository.prescription.PrescriptionMapper;
import com.medicine.repository.prescription.PrescriptionOcrRecordMapper;
import com.medicine.service.ocr.OcrService;
import com.medicine.service.storage.S3CompatibleObjectStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AppPrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(AppPrescriptionService.class);

    private static final long DEFAULT_TENANT_ID = 1L;
    private static final long MAX_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final AppAccountMapper appAccountMapper;
    private final PatientMapper patientMapper;
    private final FileAssetMapper fileAssetMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionAttachmentMapper prescriptionAttachmentMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final PrescriptionOcrRecordMapper prescriptionOcrRecordMapper;
    private final S3CompatibleObjectStorage objectStorage;
    private final ObjectStorageProperties objectStorageProperties;
    private final OcrService ocrService;
    private final ObjectMapper objectMapper;

    public AppPrescriptionService(
            AppAccountMapper appAccountMapper,
            PatientMapper patientMapper,
            FileAssetMapper fileAssetMapper,
            PrescriptionMapper prescriptionMapper,
            PrescriptionAttachmentMapper prescriptionAttachmentMapper,
            PrescriptionItemMapper prescriptionItemMapper,
            PrescriptionOcrRecordMapper prescriptionOcrRecordMapper,
            S3CompatibleObjectStorage objectStorage,
            ObjectStorageProperties objectStorageProperties,
            OcrService ocrService,
            ObjectMapper objectMapper
    ) {
        this.appAccountMapper = appAccountMapper;
        this.patientMapper = patientMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.prescriptionMapper = prescriptionMapper;
        this.prescriptionAttachmentMapper = prescriptionAttachmentMapper;
        this.prescriptionItemMapper = prescriptionItemMapper;
        this.prescriptionOcrRecordMapper = prescriptionOcrRecordMapper;
        this.objectStorage = objectStorage;
        this.objectStorageProperties = objectStorageProperties;
        this.ocrService = ocrService;
        this.objectMapper = objectMapper;
    }

    public PageResult<PrescriptionDTO> getPrescriptions(String subject, String status, int pageNo, int pageSize) {
        Patient patient = requireBoundPatient(subject);
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 50);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;
        String normalizedStatus = normalize(status);
        List<PrescriptionDTO> list = prescriptionMapper
                .searchForPatient(DEFAULT_TENANT_ID, patient.getId(), normalizedStatus, offset, normalizedPageSize)
                .stream()
                .map(this::toSummaryDto)
                .toList();
        long total = prescriptionMapper.countForPatient(DEFAULT_TENANT_ID, patient.getId(), normalizedStatus);
        return new PageResult<>(normalizedPageNo, normalizedPageSize, total, list);
    }

    @Transactional
    public PrescriptionDTO registerUpload(String subject, PrescriptionUploadRequest request) {
        if (request == null || (request.getFileAssetId() == null && !StringUtils.hasText(request.getObjectKey())) || !StringUtils.hasText(request.getOriginalFilename())
                || !StringUtils.hasText(request.getContentType()) || request.getSizeBytes() == null || request.getSizeBytes() <= 0) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        String contentType = normalizeContentType(request.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID_TYPE.getCode(), ErrorCode.FILE_UPLOAD_INVALID_TYPE.getMessage());
        }
        if (request.getSizeBytes() > MAX_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_TOO_LARGE.getCode(), ErrorCode.FILE_UPLOAD_TOO_LARGE.getMessage());
        }

        AppAccount account = requireAccount(subject);
        Patient patient = requireBoundPatient(account);

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        FileAsset fileAsset = request.getFileAssetId() == null
                ? fileAssetMapper.selectByObjectKeyAndTenant(DEFAULT_TENANT_ID, request.getObjectKey().trim())
                : fileAssetMapper.selectByIdAndTenant(DEFAULT_TENANT_ID, request.getFileAssetId());
        if (fileAsset != null) {
            if (!account.getId().equals(fileAsset.getUploadedById())
                    || !"PRESCRIPTION".equals(fileAsset.getBusinessType()) || !"PRESIGNED".equals(fileAsset.getScanStatus())
                    || !request.getOriginalFilename().trim().equals(fileAsset.getOriginalFilename())
                    || !contentType.equals(fileAsset.getMimeType()) || !request.getSizeBytes().equals(fileAsset.getSizeBytes())) {
                throw new BusinessException(400, "FILE_ASSET_INVALID");
            }
            fileAsset.setSha256(StringUtils.hasText(request.getSha256()) ? request.getSha256().trim() : null);
            fileAsset.setPublicUrl(objectStorage.createPresignedDownloadUrl(fileAsset.getObjectKey()));
            fileAsset.setScanStatus("PENDING");
            fileAsset.setUpdater("app-prescription");
            fileAsset.setUpdateTime(now);
            if (fileAssetMapper.completeRegistration(fileAsset) != 1) {
                throw new BusinessException(409, "FILE_ASSET_REGISTRATION_CONFLICT");
            }
        } else {
            if (request.getFileAssetId() != null || !request.getObjectKey().trim().startsWith("prescriptions/")) {
                throw new BusinessException(400, "FILE_ASSET_INVALID");
            }
            fileAsset = new FileAsset();
            fileAsset.setBucket(objectStorageProperties.getBucket());
            fileAsset.setObjectKey(request.getObjectKey().trim());
            fileAsset.setOriginalFilename(request.getOriginalFilename().trim());
            fileAsset.setMimeType(contentType);
            fileAsset.setSizeBytes(request.getSizeBytes());
            fileAsset.setSha256(StringUtils.hasText(request.getSha256()) ? request.getSha256().trim() : null);
            fileAsset.setStorageProvider("S3_COMPATIBLE");
            fileAsset.setPublicUrl(objectStorage.createPresignedDownloadUrl(fileAsset.getObjectKey()));
            fileAsset.setUploadedByType("PATIENT");
            fileAsset.setUploadedById(account.getId());
            fileAsset.setBusinessType("PRESCRIPTION");
            fileAsset.setScanStatus("PENDING");
            fileAsset.setCleanupAttemptCount(0);
            applyAudit(fileAsset, now, "app-prescription");
            fileAssetMapper.insert(fileAsset);
        }

        Prescription prescription = new Prescription();
        prescription.setPatientId(patient.getId());
        prescription.setSourceType("PHOTO_UPLOAD");
        prescription.setStatus("UPLOADED");
        prescription.setOcrConfidence(BigDecimal.ZERO);
        prescription.setUploadedFileAssetId(fileAsset.getId());
        prescription.setSubmittedByType("PATIENT");
        prescription.setSubmittedById(account.getId());
        applyAudit(prescription, now, "app-prescription");
        prescriptionMapper.insert(prescription);

        PrescriptionAttachment attachment = new PrescriptionAttachment();
        attachment.setPrescriptionId(prescription.getId());
        attachment.setFileAssetId(fileAsset.getId());
        attachment.setAttachmentType("ORIGINAL_IMAGE");
        applyAudit(attachment, now, "app-prescription");
        prescriptionAttachmentMapper.insert(attachment);

        OcrService.OcrResult ocrResult = performOcrFromStoredObject(fileAsset);
        PrescriptionOcrRecord ocrRecord = new PrescriptionOcrRecord();
        ocrRecord.setPrescriptionId(prescription.getId());
        if (ocrResult.isSuccess()) {
            ocrRecord.setProvider("SILICONFLOW_OCR");
            ocrRecord.setConfidence(ocrResult.getConfidence());
            boolean highConfidence = ocrResult.getConfidence().compareTo(new BigDecimal("0.80")) >= 0 && !ocrResult.getItems().isEmpty();
            ocrRecord.setStatus(highConfidence
                    ? "HIGH_CONFIDENCE" : "LOW_CONFIDENCE");
            ocrRecord.setResponsePayloadJson(ocrResult.getResponseJson());
            ocrRecord.setExtractedText(ocrResult.getExtractedText());
            ocrRecord.setStructuredItemsJson(writeStructuredItems(ocrResult.getItems()));
            ocrRecord.setManualFallbackUsed(false);
            if (highConfidence) {
                writeOcrDraftItems(prescription.getId(), ocrResult.getItems(), now);
                prescription.setStatus("OCR_DONE");
                prescription.setOcrConfidence(ocrResult.getConfidence());
                prescription.setUpdater("app-prescription");
                prescription.setUpdateTime(now);
                prescriptionMapper.updateOcrResult(prescription);
            }
        } else {
            ocrRecord.setProvider("MANUAL_REVIEW_REQUIRED");
            ocrRecord.setConfidence(BigDecimal.ZERO);
            ocrRecord.setStatus("LOW_CONFIDENCE");
            ocrRecord.setFailureReason(ocrResult.getFailureReason());
            ocrRecord.setManualFallbackUsed(true);
        }
        ocrRecord.setExecutedAt(now);
        applyAudit(ocrRecord, now, "app-prescription");
        prescriptionOcrRecordMapper.insert(ocrRecord);

        PrescriptionDTO.AttachmentDTO attachmentDTO = new PrescriptionDTO.AttachmentDTO();
        attachmentDTO.setId(attachment.getId());
        attachmentDTO.setFileAssetId(fileAsset.getId());
        attachmentDTO.setAttachmentType(attachment.getAttachmentType());
        attachmentDTO.setOriginalFilename(fileAsset.getOriginalFilename());
        attachmentDTO.setMimeType(fileAsset.getMimeType());
        attachmentDTO.setSizeBytes(fileAsset.getSizeBytes());
        attachmentDTO.setObjectKey(fileAsset.getObjectKey());
        attachmentDTO.setDownloadUrl(fileAsset.getPublicUrl());

        prescription.setPatientName(patient.getName());
        prescription.setHospitalPatientId(patient.getHospitalPatientId());
        PrescriptionDTO dto = toSummaryDto(prescription);
        dto.setAttachments(List.of(attachmentDTO));
        dto.setItems(loadItems(prescription.getId()));
        return dto;
    }

    private void writeOcrDraftItems(Long prescriptionId, List<OcrService.OcrItem> items, LocalDateTime now) {
        int sortOrder = 0;
        for (OcrService.OcrItem ocrItem : items) {
            PrescriptionItem item = new PrescriptionItem();
            item.setPrescriptionId(prescriptionId);
            item.setDrugName(ocrItem.getDrugName());
            item.setDrugSpec(ocrItem.getDrugSpec());
            item.setDosage(ocrItem.getDosage());
            item.setFrequency(ocrItem.getFrequency());
            item.setRoute(ocrItem.getRoute());
            item.setDurationDays(ocrItem.getDurationDays());
            item.setNormalizedJson(writeJson(Map.of(
                    "note", ocrItem.getNote() == null ? "" : ocrItem.getNote(),
                    "confidence", ocrItem.getConfidence() == null ? BigDecimal.ZERO : ocrItem.getConfidence()
            )));
            item.setSource("OCR");
            item.setSortOrder(sortOrder++);
            applyAudit(item, now, "app-prescription-ocr");
            prescriptionItemMapper.insert(item);
        }
    }

    private String writeStructuredItems(List<OcrService.OcrItem> items) {
        return writeJson(items);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private OcrService.OcrResult performOcrFromStoredObject(FileAsset fileAsset) {
        try {
            byte[] imageBytes = objectStorage.readObjectBytes(fileAsset.getObjectKey());
            if (imageBytes.length == 0) {
                log.warn("OCR image object is empty, objectKey={}", fileAsset.getObjectKey());
                return OcrService.OcrResult.failure("IMAGE_OBJECT_EMPTY");
            }
            return ocrService.performOcr(imageBytes, fileAsset.getMimeType());
        } catch (Exception ex) {
            log.warn("OCR image read failed, objectKey={}, reason={}", fileAsset.getObjectKey(), ex.getMessage());
            return OcrService.OcrResult.failure("IMAGE_READ_FAILED");
        }
    }

    private AppAccount requireAccount(String subject) {
        AppAccount account = appAccountMapper.selectActiveByMobile(DEFAULT_TENANT_ID, subject);
        if (account == null) {
            throw new BusinessException(401, "UNAUTHORIZED");
        }
        return account;
    }

    private Patient requireBoundPatient(String subject) {
        return requireBoundPatient(requireAccount(subject));
    }

    private Patient requireBoundPatient(AppAccount account) {
        Patient patient = patientMapper.selectByAppAccountId(DEFAULT_TENANT_ID, account.getId());
        if (patient == null || "PENDING".equals(patient.getBindingStatus())) {
            throw new BusinessException(400, "PATIENT_NOT_BOUND");
        }
        return patient;
    }

    private PrescriptionDTO toSummaryDto(Prescription prescription) {
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
        PrescriptionOcrRecord ocrRecord = prescriptionOcrRecordMapper.selectLatestByPrescriptionId(DEFAULT_TENANT_ID, prescription.getId());
        dto.setOcrStatus(ocrRecord == null ? null : ocrRecord.getStatus());
        dto.setOcrConfidence(ocrRecord == null || ocrRecord.getConfidence() == null
                ? null
                : ocrRecord.getConfidence().setScale(2, RoundingMode.HALF_UP).toPlainString());
        dto.setOcrRawText(ocrRecord == null ? null : ocrRecord.getExtractedText());
        dto.setOcrFailureReason(ocrRecord == null ? null : ocrRecord.getFailureReason());
        dto.setAttachments(loadAttachments(prescription.getId()));
        dto.setItems(loadItems(prescription.getId()));
        dto.setPendingDdiScreenings(List.of());
        return dto;
    }

    private List<PrescriptionDTO.AttachmentDTO> loadAttachments(Long prescriptionId) {
        return prescriptionAttachmentMapper.selectByPrescriptionId(DEFAULT_TENANT_ID, prescriptionId).stream().map(attachment -> {
            FileAsset fileAsset = fileAssetMapper.selectByIdAndTenant(DEFAULT_TENANT_ID, attachment.getFileAssetId());
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

    private List<PrescriptionDTO.ItemDTO> loadItems(Long prescriptionId) {
        return prescriptionItemMapper.selectByPrescriptionId(DEFAULT_TENANT_ID, prescriptionId).stream().map(item -> {
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

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeContentType(String value) {
        String normalized = value.trim().toLowerCase();
        int parameterIndex = normalized.indexOf(';');
        return parameterIndex < 0 ? normalized : normalized.substring(0, parameterIndex).trim();
    }

    private void applyAudit(BaseEntity entity, LocalDateTime now, String operator) {
        entity.setCreator(operator);
        entity.setCreateTime(now);
        entity.setUpdater(operator);
        entity.setUpdateTime(now);
        entity.setDeleted(false);
        entity.setTenantId(DEFAULT_TENANT_ID);
    }
}
