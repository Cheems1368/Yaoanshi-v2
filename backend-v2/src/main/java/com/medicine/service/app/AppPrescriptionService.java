package com.medicine.service.app;

import com.medicine.api.dto.prescription.PrescriptionDTO;
import com.medicine.api.dto.prescription.PrescriptionUploadRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.ObjectStorageProperties;
import com.medicine.domain.common.BaseEntity;
import com.medicine.domain.file.FileAsset;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.prescription.Prescription;
import com.medicine.domain.prescription.PrescriptionAttachment;
import com.medicine.domain.prescription.PrescriptionOcrRecord;
import com.medicine.repository.file.FileAssetMapper;
import com.medicine.repository.identity.AppAccountMapper;
import com.medicine.repository.identity.PatientMapper;
import com.medicine.repository.prescription.PrescriptionAttachmentMapper;
import com.medicine.repository.prescription.PrescriptionMapper;
import com.medicine.repository.prescription.PrescriptionOcrRecordMapper;
import com.medicine.service.storage.S3CompatibleObjectStorage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@Service
public class AppPrescriptionService {

    private static final long DEFAULT_TENANT_ID = 1L;
    private static final long MAX_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final AppAccountMapper appAccountMapper;
    private final PatientMapper patientMapper;
    private final FileAssetMapper fileAssetMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionAttachmentMapper prescriptionAttachmentMapper;
    private final PrescriptionOcrRecordMapper prescriptionOcrRecordMapper;
    private final S3CompatibleObjectStorage objectStorage;
    private final ObjectStorageProperties objectStorageProperties;

    public AppPrescriptionService(
            AppAccountMapper appAccountMapper,
            PatientMapper patientMapper,
            FileAssetMapper fileAssetMapper,
            PrescriptionMapper prescriptionMapper,
            PrescriptionAttachmentMapper prescriptionAttachmentMapper,
            PrescriptionOcrRecordMapper prescriptionOcrRecordMapper,
            S3CompatibleObjectStorage objectStorage,
            ObjectStorageProperties objectStorageProperties
    ) {
        this.appAccountMapper = appAccountMapper;
        this.patientMapper = patientMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.prescriptionMapper = prescriptionMapper;
        this.prescriptionAttachmentMapper = prescriptionAttachmentMapper;
        this.prescriptionOcrRecordMapper = prescriptionOcrRecordMapper;
        this.objectStorage = objectStorage;
        this.objectStorageProperties = objectStorageProperties;
    }

    public PrescriptionDTO registerUpload(String subject, PrescriptionUploadRequest request) {
        if (request == null || !StringUtils.hasText(request.getObjectKey()) || !StringUtils.hasText(request.getOriginalFilename())
                || !StringUtils.hasText(request.getContentType()) || request.getSizeBytes() == null || request.getSizeBytes() <= 0) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        String contentType = request.getContentType().trim().toLowerCase();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID_TYPE.getCode(), ErrorCode.FILE_UPLOAD_INVALID_TYPE.getMessage());
        }
        if (request.getSizeBytes() > MAX_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_TOO_LARGE.getCode(), ErrorCode.FILE_UPLOAD_TOO_LARGE.getMessage());
        }

        AppAccount account = appAccountMapper.selectActiveByMobile(DEFAULT_TENANT_ID, subject);
        if (account == null) {
            throw new BusinessException(401, "UNAUTHORIZED");
        }
        Patient patient = patientMapper.selectByAppAccountId(DEFAULT_TENANT_ID, account.getId());
        if (patient == null) {
            throw new BusinessException(400, "PATIENT_NOT_BOUND");
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        FileAsset fileAsset = new FileAsset();
        fileAsset.setBucket(objectStorageProperties.getBucket());
        fileAsset.setObjectKey(request.getObjectKey().trim());
        fileAsset.setOriginalFilename(request.getOriginalFilename().trim());
        fileAsset.setMimeType(contentType);
        fileAsset.setSizeBytes(request.getSizeBytes());
        fileAsset.setSha256(StringUtils.hasText(request.getSha256()) ? request.getSha256().trim() : null);
        fileAsset.setStorageProvider("S3_COMPATIBLE");
        fileAsset.setStorageRegion(null);
        fileAsset.setPublicUrl(objectStorage.createPresignedDownloadUrl(fileAsset.getObjectKey()));
        fileAsset.setUploadedByType("PATIENT");
        fileAsset.setUploadedById(account.getId());
        fileAsset.setBusinessType("PRESCRIPTION");
        fileAsset.setScanStatus("PENDING");
        applyAudit(fileAsset, now, "app-prescription");
        fileAssetMapper.insert(fileAsset);

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

        PrescriptionOcrRecord ocrRecord = new PrescriptionOcrRecord();
        ocrRecord.setPrescriptionId(prescription.getId());
        ocrRecord.setProvider("MANUAL_REVIEW_REQUIRED");
        ocrRecord.setConfidence(BigDecimal.ZERO);
        ocrRecord.setStatus("LOW_CONFIDENCE");
        ocrRecord.setManualFallbackUsed(false);
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

        PrescriptionDTO dto = new PrescriptionDTO();
        dto.setId(prescription.getId());
        dto.setPatientId(patient.getId());
        dto.setPatientName(patient.getName());
        dto.setHospitalPatientId(patient.getHospitalPatientId());
        dto.setStatus(prescription.getStatus());
        dto.setSourceType(prescription.getSourceType());
        dto.setSubmittedByType(prescription.getSubmittedByType());
        dto.setSubmittedById(prescription.getSubmittedById());
        dto.setCreatedAt(now.toInstant(ZoneOffset.UTC).toString());
        dto.setOcrStatus("LOW_CONFIDENCE");
        dto.setOcrConfidence("0.00");
        dto.setAttachments(List.of(attachmentDTO));
        dto.setItems(List.of());
        dto.setPendingDdiScreenings(List.of());
        return dto;
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
