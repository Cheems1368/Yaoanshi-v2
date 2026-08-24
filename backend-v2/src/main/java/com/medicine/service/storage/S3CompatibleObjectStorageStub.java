package com.medicine.service.storage;

import com.medicine.config.ObjectStorageProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Profile("test")
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
    public String createPresignedDownloadUrl(String objectKey, Duration signatureDuration) {
        long seconds = signatureDuration == null ? 3600 : signatureDuration.toSeconds();
        return baseObjectUrl(objectKey) + "?action=download&ttl=" + seconds;
    }

    @Override
    public byte[] readObjectBytes(String objectKey) {
        return ("stub-object:" + objectKey).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public void deleteObject(String objectKey) {
        // The test stub has no persistent object store.
    }

    private String baseObjectUrl(String objectKey) {
        String endpoint = org.springframework.util.StringUtils.hasText(properties.getPublicEndpoint())
                ? properties.getPublicEndpoint()
                : properties.getEndpoint();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return endpoint + "/" + properties.getBucket() + "/" + objectKey;
    }
}
