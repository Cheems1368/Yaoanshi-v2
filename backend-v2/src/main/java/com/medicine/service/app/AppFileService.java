package com.medicine.service.app;

import com.medicine.api.dto.file.FileAssetDTO;
import com.medicine.api.dto.file.FileRegisterRequest;
import com.medicine.api.dto.prescription.FilePresignRequest;
import com.medicine.api.dto.prescription.FilePresignResponse;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.ObjectStorageProperties;
import com.medicine.domain.file.FileAsset;
import com.medicine.repository.file.FileAssetMapper;
import com.medicine.service.auth.AppPatientService;
import com.medicine.service.storage.S3CompatibleObjectStorage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

@Service
public class AppFileService {

    private static final long MAX_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_AUDIO_CONTENT_TYPES = Set.of(
            "audio/webm",
            "audio/mp4",
            "audio/mpeg",
            "audio/wav",
            "audio/x-wav",
            "audio/l16",
            "application/octet-stream"
    );
    private static final Set<String> ALLOWED_BUSINESS_TYPES = Set.of(
            "PRESCRIPTION",
            "HELP_ORDER_IMAGE",
            "HELP_ORDER_AUDIO",
            "HELP_ORDER_AUDIO_TRANSCRIPT_SOURCE"
    );

    private final S3CompatibleObjectStorage objectStorage;
    private final ObjectStorageProperties objectStorageProperties;
    private final AppPatientService appPatientService;
    private final FileAssetMapper fileAssetMapper;

    public AppFileService(
            S3CompatibleObjectStorage objectStorage,
            ObjectStorageProperties objectStorageProperties,
            AppPatientService appPatientService,
            FileAssetMapper fileAssetMapper
    ) {
        this.objectStorage = objectStorage;
        this.objectStorageProperties = objectStorageProperties;
        this.appPatientService = appPatientService;
        this.fileAssetMapper = fileAssetMapper;
    }

    public FilePresignResponse presign(FilePresignRequest request) {
        validateRequest(request);

        String businessType = normalizeBusinessType(request.getBusinessType());
        String contentType = normalizeContentType(request.getContentType());
        String objectKey = resolveObjectPrefix(businessType) + "/" + LocalDate.now() + "/" + UUID.randomUUID() + resolveExtension(request.getFilename(), contentType);
        FilePresignResponse response = new FilePresignResponse();
        response.setObjectKey(objectKey);
        response.setUploadUrl(objectStorage.createPresignedUploadUrl(objectKey));
        response.setDownloadUrl("PRESCRIPTION".equals(businessType) ? objectStorage.createPresignedDownloadUrl(objectKey) : null);
        response.setBucket(objectStorageProperties.getBucket());
        response.setFilename(request.getFilename().trim());
        response.setContentType(contentType);
        response.setSizeBytes(request.getSizeBytes());
        return response;
    }

    public FilePresignResponse presign(String subject, FilePresignRequest request) {
        FilePresignResponse response = presign(request);
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        FileAsset fileAsset = new FileAsset();
        fileAsset.setBucket(response.getBucket());
        fileAsset.setObjectKey(response.getObjectKey());
        fileAsset.setOriginalFilename(response.getFilename());
        fileAsset.setMimeType(response.getContentType());
        fileAsset.setSizeBytes(response.getSizeBytes());
        fileAsset.setStorageProvider("S3_COMPATIBLE");
        fileAsset.setUploadedByType(context.operatorType());
        fileAsset.setUploadedById(context.appAccount().getId());
        fileAsset.setBusinessType(normalizeBusinessType(request.getBusinessType()));
        fileAsset.setScanStatus("PRESIGNED");
        fileAsset.setCleanupAttemptCount(0);
        applyAudit(fileAsset, "app-file-presign", now);
        fileAssetMapper.insert(fileAsset);
        response.setFileAssetId(fileAsset.getId());
        return response;
    }

    public FileAssetDTO register(String subject, FileRegisterRequest request) {
        validateRegisterRequest(request);
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        String businessType = normalizeBusinessType(request.getBusinessType());
        String contentType = normalizeContentType(request.getContentType());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        FileAsset fileAsset;
        if (request.getFileAssetId() != null) {
            fileAsset = fileAssetMapper.selectByIdAndTenant(1L, request.getFileAssetId());
            if (fileAsset == null || !context.appAccount().getId().equals(fileAsset.getUploadedById())
                    || !businessType.equals(fileAsset.getBusinessType()) || !"PRESIGNED".equals(fileAsset.getScanStatus())
                    || !request.getOriginalFilename().trim().equals(fileAsset.getOriginalFilename())
                    || !contentType.equals(fileAsset.getMimeType()) || !request.getSizeBytes().equals(fileAsset.getSizeBytes())) {
                throw new BusinessException(400, "FILE_ASSET_INVALID");
            }
            fileAsset.setSha256(StringUtils.hasText(request.getSha256()) ? request.getSha256().trim() : null);
            fileAsset.setScanStatus("PENDING");
            fileAsset.setUpdater("app-file");
            fileAsset.setUpdateTime(now);
            fileAssetMapper.completeRegistration(fileAsset);
        } else {
            fileAsset = new FileAsset();
            fileAsset.setBucket(objectStorageProperties.getBucket());
            fileAsset.setObjectKey(request.getObjectKey().trim());
            fileAsset.setOriginalFilename(request.getOriginalFilename().trim());
            fileAsset.setMimeType(contentType);
            fileAsset.setSizeBytes(request.getSizeBytes());
            fileAsset.setSha256(StringUtils.hasText(request.getSha256()) ? request.getSha256().trim() : null);
            fileAsset.setStorageProvider("S3_COMPATIBLE");
            fileAsset.setUploadedByType(context.operatorType());
            fileAsset.setUploadedById(context.appAccount().getId());
            fileAsset.setBusinessType(businessType);
            fileAsset.setScanStatus("PENDING");
            fileAsset.setCleanupAttemptCount(0);
            applyAudit(fileAsset, "app-file", now);
            fileAssetMapper.insert(fileAsset);
        }

        FileAssetDTO dto = new FileAssetDTO();
        dto.setId(fileAsset.getId());
        dto.setObjectKey(fileAsset.getObjectKey());
        dto.setOriginalFilename(fileAsset.getOriginalFilename());
        dto.setContentType(fileAsset.getMimeType());
        dto.setSizeBytes(fileAsset.getSizeBytes());
        dto.setBusinessType(fileAsset.getBusinessType());
        return dto;
    }

    private void validateRequest(FilePresignRequest request) {
        if (request == null || !StringUtils.hasText(request.getFilename()) || !StringUtils.hasText(request.getContentType())
                || request.getSizeBytes() == null || request.getSizeBytes() <= 0) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        if (!ALLOWED_BUSINESS_TYPES.contains(normalizeBusinessType(request.getBusinessType()))) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID_TYPE.getCode(), ErrorCode.FILE_UPLOAD_INVALID_TYPE.getMessage());
        }
        String contentType = normalizeContentType(request.getContentType());
        if (!isAllowedContentType(normalizeBusinessType(request.getBusinessType()), contentType)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID_TYPE.getCode(), ErrorCode.FILE_UPLOAD_INVALID_TYPE.getMessage());
        }
        if (request.getSizeBytes() > MAX_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_TOO_LARGE.getCode(), ErrorCode.FILE_UPLOAD_TOO_LARGE.getMessage());
        }
    }

    private void validateRegisterRequest(FileRegisterRequest request) {
        if (request == null || (request.getFileAssetId() == null && !StringUtils.hasText(request.getObjectKey())) || !StringUtils.hasText(request.getOriginalFilename())
                || !StringUtils.hasText(request.getContentType()) || request.getSizeBytes() == null || request.getSizeBytes() <= 0) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        String businessType = normalizeBusinessType(request.getBusinessType());
        if (!"HELP_ORDER_IMAGE".equals(businessType)
                && !"HELP_ORDER_AUDIO".equals(businessType)
                && !"HELP_ORDER_AUDIO_TRANSCRIPT_SOURCE".equals(businessType)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID_TYPE.getCode(), ErrorCode.FILE_UPLOAD_INVALID_TYPE.getMessage());
        }
        validateRequest(toPresignRequest(request));
        if (request.getFileAssetId() == null && !request.getObjectKey().trim().startsWith(resolveObjectPrefix(businessType) + "/")) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID_TYPE.getCode(), ErrorCode.FILE_UPLOAD_INVALID_TYPE.getMessage());
        }
    }

    private FilePresignRequest toPresignRequest(FileRegisterRequest request) {
        FilePresignRequest presignRequest = new FilePresignRequest();
        presignRequest.setFilename(request.getOriginalFilename());
        presignRequest.setContentType(request.getContentType());
        presignRequest.setSizeBytes(request.getSizeBytes());
        presignRequest.setBusinessType(request.getBusinessType());
        return presignRequest;
    }

    private String normalizeBusinessType(String businessType) {
        return StringUtils.hasText(businessType) ? businessType.trim().toUpperCase() : "";
    }

    private String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        String normalized = contentType.trim().toLowerCase();
        int parameterIndex = normalized.indexOf(';');
        if (parameterIndex >= 0) {
            normalized = normalized.substring(0, parameterIndex).trim();
        }
        return normalized;
    }

    private String resolveObjectPrefix(String businessType) {
        return switch (businessType) {
            case "HELP_ORDER_IMAGE" -> "help-orders/images";
            case "HELP_ORDER_AUDIO" -> "help-orders/audio";
            case "HELP_ORDER_AUDIO_TRANSCRIPT_SOURCE" -> "help-orders/audio-transcript-source";
            default -> "prescriptions";
        };
    }

    private String resolveExtension(String filename, String contentType) {
        String trimmed = filename == null ? "" : filename.trim();
        int dotIndex = trimmed.lastIndexOf('.');
        if (dotIndex > -1 && dotIndex < trimmed.length() - 1) {
            return trimmed.substring(dotIndex);
        }
        return switch (contentType.trim().toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "audio/webm" -> ".webm";
            case "audio/mp4" -> ".m4a";
            case "audio/mpeg" -> ".mp3";
            case "audio/wav", "audio/x-wav", "audio/l16", "application/octet-stream" -> ".wav";
            default -> ".jpg";
        };
    }

    private boolean isAllowedContentType(String businessType, String contentType) {
        if ("HELP_ORDER_AUDIO".equals(businessType) || "HELP_ORDER_AUDIO_TRANSCRIPT_SOURCE".equals(businessType)) {
            return ALLOWED_AUDIO_CONTENT_TYPES.contains(contentType);
        }
        return ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType);
    }

    private void applyAudit(com.medicine.domain.common.BaseEntity entity, String operator, LocalDateTime now) {
        entity.setCreator(operator);
        entity.setCreateTime(now);
        entity.setUpdater(operator);
        entity.setUpdateTime(now);
        entity.setDeleted(false);
        entity.setTenantId(1L);
    }
}
