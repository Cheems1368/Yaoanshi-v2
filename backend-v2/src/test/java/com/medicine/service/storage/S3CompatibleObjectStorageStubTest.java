package com.medicine.service.storage;

import com.medicine.config.ObjectStorageProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class S3CompatibleObjectStorageStubTest {

    @Test
    void shouldComposeUploadAndDownloadUrlsFromConfiguredEndpoint() {
        ObjectStorageProperties properties = new ObjectStorageProperties();
        properties.setEndpoint("http://localhost:9000");
        properties.setBucket("yaoshian-mvp");
        S3CompatibleObjectStorageStub storage = new S3CompatibleObjectStorageStub(properties);

        assertThat(storage.createPresignedUploadUrl("prescriptions/a.png"))
                .isEqualTo("http://localhost:9000/yaoshian-mvp/prescriptions/a.png?action=upload");
        assertThat(storage.createPresignedDownloadUrl("prescriptions/a.png"))
                .isEqualTo("http://localhost:9000/yaoshian-mvp/prescriptions/a.png?action=download");
    }

    @Test
    void shouldTrimTrailingSlashFromEndpoint() {
        ObjectStorageProperties properties = new ObjectStorageProperties();
        properties.setEndpoint("http://localhost:9000/");
        properties.setBucket("yaoshian-mvp");
        S3CompatibleObjectStorageStub storage = new S3CompatibleObjectStorageStub(properties);

        assertThat(storage.createPresignedUploadUrl("prescriptions/b.png"))
                .isEqualTo("http://localhost:9000/yaoshian-mvp/prescriptions/b.png?action=upload");
    }
}
