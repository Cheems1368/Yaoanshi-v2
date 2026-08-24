package com.medicine.service.storage;

import java.time.Duration;

public interface S3CompatibleObjectStorage {

    String createPresignedUploadUrl(String objectKey);

    default String createPresignedDownloadUrl(String objectKey) {
        return createPresignedDownloadUrl(objectKey, Duration.ofHours(1));
    }

    String createPresignedDownloadUrl(String objectKey, Duration signatureDuration);

    byte[] readObjectBytes(String objectKey);

    void deleteObject(String objectKey);
}
