package com.medicine.service.app;

import com.medicine.api.dto.prescription.FilePresignRequest;
import com.medicine.api.dto.prescription.FilePresignResponse;
import com.medicine.common.exception.BusinessException;
import com.medicine.config.ObjectStorageProperties;
import com.medicine.service.storage.S3CompatibleObjectStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppFileServiceTest {

    @Mock
    private S3CompatibleObjectStorage objectStorage;

    @Spy
    private ObjectStorageProperties objectStorageProperties = new ObjectStorageProperties();

    @InjectMocks
    private AppFileService appFileService;

    @Test
    void shouldCreatePresignPayloadForPrescriptionImage() {
        when(objectStorage.createPresignedUploadUrl(anyString())).thenAnswer(invocation -> "upload:" + invocation.getArgument(0));
        when(objectStorage.createPresignedDownloadUrl(anyString())).thenAnswer(invocation -> "download:" + invocation.getArgument(0));

        FilePresignRequest request = new FilePresignRequest();
        request.setFilename("rx-photo.jpg");
        request.setContentType("image/jpeg");
        request.setSizeBytes(1024L);
        request.setBusinessType("PRESCRIPTION");

        FilePresignResponse response = appFileService.presign(request);

        assertThat(response.getObjectKey()).contains("prescriptions/");
        assertThat(response.getUploadUrl()).startsWith("upload:");
        assertThat(response.getDownloadUrl()).startsWith("download:");
    }

    @Test
    void shouldRejectUnsupportedContentType() {
        FilePresignRequest request = new FilePresignRequest();
        request.setFilename("rx.pdf");
        request.setContentType("application/pdf");
        request.setSizeBytes(1024L);
        request.setBusinessType("PRESCRIPTION");

        assertThatThrownBy(() -> appFileService.presign(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("FILE_UPLOAD_INVALID_TYPE");
    }
}
