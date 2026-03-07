package com.medicine.service.app;

import com.medicine.api.dto.prescription.PrescriptionDTO;
import com.medicine.api.dto.prescription.PrescriptionUploadRequest;
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
import com.medicine.repository.prescription.PrescriptionMapper;
import com.medicine.repository.prescription.PrescriptionOcrRecordMapper;
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
    private PrescriptionOcrRecordMapper prescriptionOcrRecordMapper;

    @Mock
    private S3CompatibleObjectStorage objectStorage;

    @Spy
    private ObjectStorageProperties objectStorageProperties = new ObjectStorageProperties();

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

        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(account);
        when(patientMapper.selectByAppAccountId(1L, 101L)).thenReturn(patient);
        when(objectStorage.createPresignedDownloadUrl("prescriptions/mock.jpg")).thenReturn("download-url");

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
}
