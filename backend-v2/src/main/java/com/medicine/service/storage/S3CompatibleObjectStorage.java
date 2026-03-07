package com.medicine.service.storage;

public interface S3CompatibleObjectStorage {

    String createPresignedUploadUrl(String objectKey);

    String createPresignedDownloadUrl(String objectKey);
}
