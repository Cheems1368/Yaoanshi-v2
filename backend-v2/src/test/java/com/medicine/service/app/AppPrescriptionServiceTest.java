package com.medicine.service.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.prescription.PrescriptionDTO;
import com.medicine.api.dto.prescription.PrescriptionUploadRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.model.PageResult;
import com.medicine.config.ObjectStorageProperties;
import com.medicine.domain.file.FileAsset;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.prescription.Prescription;
import com.medicine.domain.prescription.PrescriptionAttachment;
import com.medicine.domain.prescription.PrescriptionOcrRecord;
import com.medicine.repository.file.FileAssetMapper;
import com.medicine.repository.identity.AppAccountMapper;
import com.medicine.repository.identity.PatientMapper;
import com.medicine.repository.prescription.PrescriptionAttachmentMapper;
import com.medicine.repository.prescription.PrescriptionItemMapper;
import com.medicine.repository.prescription.PrescriptionMapper;
import com.medicine.repository.prescription.PrescriptionOcrRecordMapper;
import com.medicine.service.ocr.OcrService;
import com.medicine.service.storage.S3CompatibleObjectStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.medicine.domain.prescription.PrescriptionItem;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AppPrescriptionServiceTest {

    @Mock
    private AppAccountMapper appAccountMapper;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private FileAssetMapper fileAssetMapper;

    @Mock
    private PrescriptionMapper prescriptionMapper;

    @Mock
    private PrescriptionAttachmentMapper prescriptionAttachmentMapper;

    @Mock
    private PrescriptionItemMapper prescriptionItemMapper;

    @Mock
    private PrescriptionOcrRecordMapper prescriptionOcrRecordMapper;

    @Mock
    private S3CompatibleObjectStorage objectStorage;

    @Mock
    private OcrService ocrService;

    @Spy
    private ObjectStorageProperties objectStorageProperties = new ObjectStorageProperties();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AppPrescriptionService appPrescriptionService;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldPersistLocalProviderAndSafeMedicationText(boolean prefilled) {
        AppAccount account = new AppAccount();
        account.setId(101L);
        Patient patient = new Patient();
        patient.setId(201L);
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(account);
        when(patientMapper.selectByAppAccountId(1L, 101L)).thenReturn(patient);
        when(objectStorage.readObjectBytes("prescriptions/local.png")).thenReturn(new byte[]{1});
        doAnswer(invocation -> { ((FileAsset) invocation.getArgument(0)).setId(301L); return 1; })
                .when(fileAssetMapper).insert(any(FileAsset.class));
        doAnswer(invocation -> { ((Prescription) invocation.getArgument(0)).setId(401L); return 1; })
                .when(prescriptionMapper).insert(any(Prescription.class));
        OcrService.OcrItem item = new OcrService.OcrItem();
        item.setDrugName("二甲双胍片");
        item.setDrugSpec("0.5g/片");
        item.setConfidence(new BigDecimal("0.96"));
        when(ocrService.performOcr(any(byte[].class), any())).thenReturn(prefilled
                ? OcrService.OcrResult.success("二甲双胍片 0.5g/片", List.of(item), new BigDecimal("0.96"), "{}")
                : OcrService.OcrResult.reviewRequired("二甲双胍片 0.5g/片", "OCR_MEDICATION_LOW_CONFIDENCE"));
        PrescriptionUploadRequest request = new PrescriptionUploadRequest();
        request.setObjectKey("prescriptions/local.png");
        request.setOriginalFilename("local.png");
        request.setContentType("image/png");
        request.setSizeBytes(1L);
        PrescriptionDTO dto = appPrescriptionService.registerUpload("13800138000", request);
        ArgumentCaptor<PrescriptionOcrRecord> record = ArgumentCaptor.forClass(PrescriptionOcrRecord.class);
        verify(prescriptionOcrRecordMapper).insert(record.capture());
        assertThat(record.getValue().getProvider()).isEqualTo("RAPIDOCR_LOCAL");
        assertThat(record.getValue().getExtractedText()).isEqualTo("二甲双胍片 0.5g/片");
        assertThat(record.getValue().getManualFallbackUsed()).isEqualTo(!prefilled);
        if (prefilled) {
            assertThat(dto.getStatus()).isEqualTo("OCR_DONE");
            ArgumentCaptor<PrescriptionItem> draft = ArgumentCaptor.forClass(PrescriptionItem.class);
            verify(prescriptionItemMapper).insert(draft.capture());
            assertThat(draft.getValue().getDosage()).isNull();
            assertThat(draft.getValue().getSource()).isEqualTo("OCR");
        } else {
            assertThat(dto.getStatus()).isEqualTo("UPLOADED");
            assertThat(record.getValue().getFailureReason()).isEqualTo("OCR_MEDICATION_LOW_CONFIDENCE");
            verify(prescriptionItemMapper, never()).insert(any(PrescriptionItem.class));
        }
    }

    @Test
    void shouldRejectPrescriptionHistoryUntilPharmacistConfirmsBinding() {
        AppAccount account = new AppAccount();
        account.setId(101L);
        Patient patient = new Patient();
        patient.setBindingStatus("PENDING");
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(account);
        when(patientMapper.selectByAppAccountId(1L, 101L)).thenReturn(patient);

        assertThatThrownBy(() -> appPrescriptionService.getPrescriptions("13800138000", null, 1, 20))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(prescriptionMapper);
    }

    @Test
    void shouldRegisterPrescriptionUploadFromBoundPatient() {
        AppAccount account = new AppAccount();
        account.setId(101L);
        account.setMobile("13800138000");
        Patient patient = new Patient();
        patient.setId(201L);
        patient.setName("Test Patient");
        patient.setHospitalPatientId("HZY-1001");
        PrescriptionOcrRecord latestOcrRecord = new PrescriptionOcrRecord();
        latestOcrRecord.setStatus("LOW_CONFIDENCE");
        latestOcrRecord.setConfidence(java.math.BigDecimal.ZERO);

        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(account);
        when(patientMapper.selectByAppAccountId(1L, 101L)).thenReturn(patient);
        FileAsset fileAsset = new FileAsset();
        fileAsset.setId(301L); fileAsset.setTenantId(1L); fileAsset.setObjectKey("prescriptions/mock.jpg");
        fileAsset.setOriginalFilename("mock.jpg"); fileAsset.setMimeType("image/jpeg"); fileAsset.setSizeBytes(2048L);
        fileAsset.setUploadedById(101L); fileAsset.setBusinessType("PRESCRIPTION"); fileAsset.setScanStatus("PRESIGNED");
        when(fileAssetMapper.selectByObjectKeyAndTenant(1L, "prescriptions/mock.jpg")).thenReturn(fileAsset);
        when(fileAssetMapper.completeRegistration(fileAsset)).thenReturn(1);
        when(objectStorage.createPresignedDownloadUrl("prescriptions/mock.jpg")).thenReturn("download-url");
        when(objectStorage.readObjectBytes("prescriptions/mock.jpg")).thenReturn("image-bytes".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        when(ocrService.performOcr(any(byte[].class), any())).thenReturn(OcrService.OcrResult.failure(""));

        doAnswer(invocation -> {
            Prescription prescription = invocation.getArgument(0);
            prescription.setId(401L);
            return 1;
        }).when(prescriptionMapper).insert(any(Prescription.class));
        doAnswer(invocation -> {
            PrescriptionAttachment attachment = invocation.getArgument(0);
            attachment.setId(501L);
            return 1;
        }).when(prescriptionAttachmentMapper).insert(any(PrescriptionAttachment.class));
        doAnswer(invocation -> {
            PrescriptionOcrRecord record = invocation.getArgument(0);
            record.setId(601L);
            return 1;
        }).when(prescriptionOcrRecordMapper).insert(any(PrescriptionOcrRecord.class));
        when(prescriptionOcrRecordMapper.selectLatestByPrescriptionId(1L, 401L)).thenReturn(latestOcrRecord);
        when(prescriptionItemMapper.selectByPrescriptionId(1L, 401L)).thenReturn(java.util.List.of());

        PrescriptionUploadRequest request = new PrescriptionUploadRequest();
        request.setObjectKey("prescriptions/mock.jpg");
        request.setOriginalFilename("mock.jpg");
        request.setContentType("image/jpeg");
        request.setSizeBytes(2048L);

        PrescriptionDTO dto = appPrescriptionService.registerUpload("13800138000", request);

        assertThat(dto.getId()).isEqualTo(401L);
        assertThat(dto.getStatus()).isEqualTo("UPLOADED");
        assertThat(dto.getAttachments()).hasSize(1);
        assertThat(dto.getOcrStatus()).isEqualTo("LOW_CONFIDENCE");
        verify(fileAssetMapper).completeRegistration(fileAsset);
        verify(fileAssetMapper, never()).insert(any(FileAsset.class));
    }

    @Test
    void shouldRejectPrescriptionWhenPresignedMetadataDoesNotMatch() {
        AppAccount account = new AppAccount();
        account.setId(101L);
        Patient patient = new Patient();
        patient.setId(201L);
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(account);
        when(patientMapper.selectByAppAccountId(1L, 101L)).thenReturn(patient);
        FileAsset fileAsset = new FileAsset();
        fileAsset.setId(301L); fileAsset.setTenantId(1L); fileAsset.setObjectKey("prescriptions/mock.jpg");
        fileAsset.setOriginalFilename("mock.jpg"); fileAsset.setMimeType("image/jpeg"); fileAsset.setSizeBytes(2048L);
        fileAsset.setUploadedById(101L); fileAsset.setBusinessType("PRESCRIPTION"); fileAsset.setScanStatus("PRESIGNED");
        when(fileAssetMapper.selectByIdAndTenant(1L, 301L)).thenReturn(fileAsset);
        PrescriptionUploadRequest request = new PrescriptionUploadRequest();
        request.setFileAssetId(301L); request.setOriginalFilename("mock.jpg"); request.setContentType("image/jpeg");
        request.setSizeBytes(2049L);

        assertThatThrownBy(() -> appPrescriptionService.registerUpload("13800138000", request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("FILE_ASSET_INVALID");
        verify(fileAssetMapper, never()).completeRegistration(any(FileAsset.class));
    }

    @Test
    void shouldReturnPrescriptionHistoryForBoundPatient() {
        AppAccount account = new AppAccount();
        account.setId(101L);
        account.setMobile("13800138000");
        Patient patient = new Patient();
        patient.setId(201L);
        patient.setName("Test Patient");
        patient.setHospitalPatientId("HZY-1001");

        Prescription prescription = new Prescription();
        prescription.setId(401L);
        prescription.setPatientId(201L);
        prescription.setPatientName("Test Patient");
        prescription.setHospitalPatientId("HZY-1001");
        prescription.setStatus("UPLOADED");
        prescription.setSourceType("PHOTO_UPLOAD");
        prescription.setSubmittedByType("PATIENT");
        prescription.setSubmittedById(101L);

        PrescriptionAttachment attachment = new PrescriptionAttachment();
        attachment.setId(501L);
        attachment.setPrescriptionId(401L);
        attachment.setFileAssetId(301L);
        attachment.setAttachmentType("ORIGINAL_IMAGE");

        FileAsset fileAsset = new FileAsset();
        fileAsset.setId(301L);
        fileAsset.setOriginalFilename("mock.jpg");
        fileAsset.setMimeType("image/jpeg");
        fileAsset.setSizeBytes(2048L);
        fileAsset.setObjectKey("prescriptions/mock.jpg");
        fileAsset.setPublicUrl("download-url");

        PrescriptionOcrRecord ocrRecord = new PrescriptionOcrRecord();
        ocrRecord.setStatus("LOW_CONFIDENCE");
        ocrRecord.setConfidence(java.math.BigDecimal.ZERO);

        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(account);
        when(patientMapper.selectByAppAccountId(1L, 101L)).thenReturn(patient);
        when(prescriptionMapper.searchForPatient(1L, 201L, null, 0, 20)).thenReturn(java.util.List.of(prescription));
        when(prescriptionMapper.countForPatient(1L, 201L, null)).thenReturn(1L);
        when(prescriptionAttachmentMapper.selectByPrescriptionId(1L, 401L)).thenReturn(java.util.List.of(attachment));
        when(fileAssetMapper.selectByIdAndTenant(1L, 301L)).thenReturn(fileAsset);
        when(prescriptionOcrRecordMapper.selectLatestByPrescriptionId(1L, 401L)).thenReturn(ocrRecord);
        when(prescriptionItemMapper.selectByPrescriptionId(1L, 401L)).thenReturn(java.util.List.of());

        PageResult<PrescriptionDTO> result = appPrescriptionService.getPrescriptions("13800138000", null, 1, 20);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getId()).isEqualTo(401L);
        assertThat(result.getList().get(0).getAttachments()).hasSize(1);
        assertThat(result.getList().get(0).getAttachments().get(0).getOriginalFilename()).isEqualTo("mock.jpg");
    }
}
