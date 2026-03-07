package com.medicine.service.storage;

import com.medicine.config.ObjectStorageProperties;
import org.springframework.stereotype.Service;

@Service
public class S3CompatibleObjectStorageStub implements S3CompatibleObjectStorage {

    private final ObjectStorageProperties properties;

    public S3CompatibleObjectStorageStub(ObjectStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String createPresignedUploadUrl(String objectKey) {
        return properties.getEndpoint() + "/" + properties.getBucket() + "/" + objectKey + "?action=upload";
    }

    @Override
    public String createPresignedDownloadUrl(String objectKey) {
        return properties.getEndpoint() + "/" + properties.getBucket() + "/" + objectKey + "?action=download";
    }
}
