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
        return baseObjectUrl(objectKey) + "?action=upload";
    }

    @Override
    public String createPresignedDownloadUrl(String objectKey) {
        return baseObjectUrl(objectKey) + "?action=download";
    }

    private String baseObjectUrl(String objectKey) {
        String endpoint = properties.getEndpoint();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return endpoint + "/" + properties.getBucket() + "/" + objectKey;
    }
}
