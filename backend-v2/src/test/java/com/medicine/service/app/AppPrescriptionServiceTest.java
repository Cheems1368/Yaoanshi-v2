package com.medicine.service.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.prescription.PrescriptionDTO;
import com.medicine.api.dto.prescription.PrescriptionUploadRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

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
        when(objectStorage.createPresignedDownloadUrl("prescriptions/mock.jpg")).thenReturn("download-url");
        when(objectStorage.readObjectBytes("prescriptions/mock.jpg")).thenReturn("image-bytes".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        when(ocrService.performOcr(any(byte[].class), any())).thenReturn(OcrService.OcrResult.failure(""));

        doAnswer(invocation -> {
            FileAsset fileAsset = invocation.getArgument(0);
            fileAsset.setId(301L);
            return 1;
        }).when(fileAssetMapper).insert(any(FileAsset.class));
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
