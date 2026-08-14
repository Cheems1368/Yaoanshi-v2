package com.medicine.service.app;

import com.medicine.api.dto.file.FileRegisterRequest;
import com.medicine.api.dto.prescription.FilePresignRequest;
import com.medicine.api.dto.prescription.FilePresignResponse;
import com.medicine.common.exception.BusinessException;
import com.medicine.config.ObjectStorageProperties;
import com.medicine.domain.file.FileAsset;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.repository.file.FileAssetMapper;
import com.medicine.service.auth.AppPatientService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppFileServiceTest {

    @Mock
    private S3CompatibleObjectStorage objectStorage;

    @Spy
    private ObjectStorageProperties objectStorageProperties = new ObjectStorageProperties();

    @Mock
    private AppPatientService appPatientService;

    @Mock
    private FileAssetMapper fileAssetMapper;

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

    @Test
    void shouldPresignHelpOrderImageWithoutDownloadUrl() {
        when(objectStorage.createPresignedUploadUrl(anyString())).thenAnswer(invocation -> "upload:" + invocation.getArgument(0));

        FilePresignRequest request = new FilePresignRequest();
        request.setFilename("bruise.png");
        request.setContentType("image/png");
        request.setSizeBytes(2048L);
        request.setBusinessType("HELP_ORDER_IMAGE");

        FilePresignResponse response = appFileService.presign(request);

        assertThat(response.getObjectKey()).contains("help-orders/images/");
        assertThat(response.getUploadUrl()).startsWith("upload:");
        assertThat(response.getDownloadUrl()).isNull();
    }

    @Test
    void shouldPresignHelpOrderAudioSourceWithoutDownloadUrl() {
        when(objectStorage.createPresignedUploadUrl(anyString())).thenAnswer(invocation -> "upload:" + invocation.getArgument(0));

        FilePresignRequest request = new FilePresignRequest();
        request.setFilename("voice-source.wav");
        request.setContentType("audio/wav");
        request.setSizeBytes(16000L);
        request.setBusinessType("HELP_ORDER_AUDIO_TRANSCRIPT_SOURCE");

        FilePresignResponse response = appFileService.presign(request);

        assertThat(response.getObjectKey()).contains("help-orders/audio-transcript-source/");
        assertThat(response.getUploadUrl()).startsWith("upload:");
        assertThat(response.getDownloadUrl()).isNull();
    }

    @Test
    void shouldNormalizeParameterizedAudioContentTypeForPresignAndRegister() {
        when(objectStorage.createPresignedUploadUrl(anyString())).thenAnswer(invocation -> "upload:" + invocation.getArgument(0));
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(fileAssetMapper.insert(any(FileAsset.class))).thenAnswer(invocation -> {
            FileAsset fileAsset = invocation.getArgument(0);
            fileAsset.setId(99L);
            return 1;
        });

        FilePresignRequest presignRequest = new FilePresignRequest();
        presignRequest.setFilename("voice.webm");
        presignRequest.setContentType("audio/webm;codecs=opus");
        presignRequest.setSizeBytes(4096L);
        presignRequest.setBusinessType("HELP_ORDER_AUDIO");

        FilePresignResponse presignResponse = appFileService.presign(presignRequest);

        assertThat(presignResponse.getObjectKey()).contains("help-orders/audio/");
        assertThat(presignResponse.getContentType()).isEqualTo("audio/webm");
        assertThat(presignResponse.getUploadUrl()).startsWith("upload:");
        assertThat(presignResponse.getDownloadUrl()).isNull();

        FileRegisterRequest registerRequest = new FileRegisterRequest();
        registerRequest.setObjectKey(presignResponse.getObjectKey());
        registerRequest.setOriginalFilename("voice.webm");
        registerRequest.setContentType("audio/webm;codecs=opus");
        registerRequest.setSizeBytes(4096L);
        registerRequest.setBusinessType("HELP_ORDER_AUDIO");

        var result = appFileService.register("13800138000", registerRequest);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getContentType()).isEqualTo("audio/webm");
        assertThat(result.getBusinessType()).isEqualTo("HELP_ORDER_AUDIO");
        verify(fileAssetMapper).insert(any(FileAsset.class));
    }

    @Test
    void shouldRejectParameterizedUnsupportedContentType() {
        FilePresignRequest request = new FilePresignRequest();
        request.setFilename("rx.pdf");
        request.setContentType("application/pdf; charset=utf-8");
        request.setSizeBytes(1024L);
        request.setBusinessType("PRESCRIPTION");

        assertThatThrownBy(() -> appFileService.presign(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("FILE_UPLOAD_INVALID_TYPE");
    }

    @Test
    void shouldRegisterHelpOrderImageForCurrentAppAccount() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(fileAssetMapper.insert(any(FileAsset.class))).thenAnswer(invocation -> {
            FileAsset fileAsset = invocation.getArgument(0);
            fileAsset.setId(77L);
            return 1;
        });

        FileRegisterRequest request = new FileRegisterRequest();
        request.setObjectKey("help-orders/images/2026-05-12/abc.png");
        request.setOriginalFilename("bruise.png");
        request.setContentType("image/png");
        request.setSizeBytes(2048L);
        request.setBusinessType("HELP_ORDER_IMAGE");

        var result = appFileService.register("13800138000", request);

        assertThat(result.getId()).isEqualTo(77L);
        assertThat(result.getBusinessType()).isEqualTo("HELP_ORDER_IMAGE");
        verify(fileAssetMapper).insert(any(FileAsset.class));
    }

    @Test
    void shouldRegisterHelpOrderPlaybackAudioForCurrentAppAccount() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(fileAssetMapper.insert(any(FileAsset.class))).thenAnswer(invocation -> {
            FileAsset fileAsset = invocation.getArgument(0);
            fileAsset.setId(88L);
            return 1;
        });

        FileRegisterRequest request = new FileRegisterRequest();
        request.setObjectKey("help-orders/audio/2026-05-12/voice.webm");
        request.setOriginalFilename("voice.webm");
        request.setContentType("audio/webm");
        request.setSizeBytes(4096L);
        request.setBusinessType("HELP_ORDER_AUDIO");

        var result = appFileService.register("13800138000", request);

        assertThat(result.getId()).isEqualTo(88L);
        assertThat(result.getBusinessType()).isEqualTo("HELP_ORDER_AUDIO");
        verify(fileAssetMapper).insert(any(FileAsset.class));
    }

    private AppPatientService.AccessContext accessContext() {
        AppAccount account = new AppAccount();
        account.setId(101L);
        account.setAccountType("PATIENT");

        Patient patient = new Patient();
        patient.setId(201L);

        return new AppPatientService.AccessContext(account, patient, "PATIENT");
    }
}
