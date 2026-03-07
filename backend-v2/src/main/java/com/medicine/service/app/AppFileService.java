package com.medicine.service.app;

import com.medicine.api.dto.prescription.FilePresignRequest;
import com.medicine.api.dto.prescription.FilePresignResponse;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.ObjectStorageProperties;
import com.medicine.service.storage.S3CompatibleObjectStorage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
public class AppFileService {

    private static final long MAX_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final S3CompatibleObjectStorage objectStorage;
    private final ObjectStorageProperties objectStorageProperties;

    public AppFileService(S3CompatibleObjectStorage objectStorage, ObjectStorageProperties objectStorageProperties) {
        this.objectStorage = objectStorage;
        this.objectStorageProperties = objectStorageProperties;
    }

    public FilePresignResponse presign(FilePresignRequest request) {
        validateRequest(request);

        String objectKey = "prescriptions/" + LocalDate.now() + "/" + UUID.randomUUID() + resolveExtension(request.getFilename(), request.getContentType());
        FilePresignResponse response = new FilePresignResponse();
        response.setObjectKey(objectKey);
        response.setUploadUrl(objectStorage.createPresignedUploadUrl(objectKey));
        response.setDownloadUrl(objectStorage.createPresignedDownloadUrl(objectKey));
        response.setBucket(objectStorageProperties.getBucket());
        response.setFilename(request.getFilename().trim());
        response.setContentType(request.getContentType().trim().toLowerCase());
        response.setSizeBytes(request.getSizeBytes());
        return response;
    }

    private void validateRequest(FilePresignRequest request) {
        if (request == null || !StringUtils.hasText(request.getFilename()) || !StringUtils.hasText(request.getContentType())
                || request.getSizeBytes() == null || request.getSizeBytes() <= 0) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        if (!"PRESCRIPTION".equalsIgnoreCase(request.getBusinessType())) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID_TYPE.getCode(), ErrorCode.FILE_UPLOAD_INVALID_TYPE.getMessage());
        }
        String contentType = request.getContentType().trim().toLowerCase();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID_TYPE.getCode(), ErrorCode.FILE_UPLOAD_INVALID_TYPE.getMessage());
        }
        if (request.getSizeBytes() > MAX_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_TOO_LARGE.getCode(), ErrorCode.FILE_UPLOAD_TOO_LARGE.getMessage());
        }
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
            default -> ".jpg";
        };
    }
}
