package com.medicine.service.admin;

import com.medicine.api.dto.prescription.DdiScreeningConfirmRequest;
import com.medicine.api.dto.prescription.PrescriptionConfirmRequest;
import com.medicine.api.dto.prescription.PrescriptionDTO;
import com.medicine.api.dto.prescription.PrescriptionRejectRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.model.PageResult;
import com.medicine.domain.ddi.DdiRule;
import com.medicine.domain.ddi.DdiScreeningLog;
import com.medicine.domain.file.FileAsset;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.domain.prescription.Prescription;
import com.medicine.domain.prescription.PrescriptionAttachment;
import com.medicine.domain.prescription.PrescriptionItem;
import com.medicine.domain.prescription.PrescriptionOcrRecord;
import com.medicine.repository.ddi.DdiRuleMapper;
import com.medicine.repository.ddi.DdiScreeningLogMapper;
import com.medicine.repository.file.FileAssetMapper;
import com.medicine.repository.prescription.PrescriptionAttachmentMapper;
import com.medicine.repository.prescription.PrescriptionItemMapper;
import com.medicine.repository.prescription.PrescriptionMapper;
import com.medicine.repository.prescription.PrescriptionOcrRecordMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPrescriptionServiceTest {

    @Mock
    private PrescriptionMapper prescriptionMapper;

    @Mock
    private PrescriptionItemMapper prescriptionItemMapper;

    @Mock
    private PrescriptionAttachmentMapper prescriptionAttachmentMapper;

    @Mock
    private PrescriptionOcrRecordMapper prescriptionOcrRecordMapper;

    @Mock
    private FileAssetMapper fileAssetMapper;

    @Mock
    private DdiRuleMapper ddiRuleMapper;

    @Mock
    private DdiScreeningLogMapper ddiScreeningLogMapper;

    @Mock
    private ProfessionalIdentityService professionalIdentityService;

    @InjectMocks
    private AdminPrescriptionService adminPrescriptionService;

    @Test
    void shouldReturnPagedPrescriptionList() {
        JwtClaims claims = claims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile());
        when(prescriptionMapper.searchForAdmin(1L, null, null, 0, 10)).thenReturn(List.of(prescription()));
        when(prescriptionMapper.countForAdmin(1L, null, null)).thenReturn(1L);
        when(prescriptionOcrRecordMapper.selectLatestByPrescriptionId(1L, 401L)).thenReturn(ocrRecord("LOW_CONFIDENCE"));

        PageResult<PrescriptionDTO> page = adminPrescriptionService.getPrescriptions(claims, null, null, 1, 10);

        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getList()).hasSize(1);
        assertThat(page.getList().get(0).getPatientName()).isEqualTo("Test Patient");
    }

    @Test
    void shouldBlockConfirmationWhenHighRiskDdiNeedsConfirmation() {
        JwtClaims claims = claims();
        Prescription prescription = prescription();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile());
        when(prescriptionMapper.selectByIdAndTenant(1L, 401L)).thenReturn(prescription);
        when(ddiRuleMapper.selectActiveRules(1L)).thenReturn(List.of(highRiskRule()));
        when(ddiScreeningLogMapper.selectByPrescriptionId(1L, 401L)).thenReturn(List.of());

        PrescriptionConfirmRequest request = confirmRequest("Aspirin", "Warfarin");

        assertThatThrownBy(() -> adminPrescriptionService.confirmPrescription(claims, 401L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("DDI_CONFIRM_REQUIRED");
    }

    @Test
    void shouldConfirmPrescriptionAfterDdiReviewed() {
        JwtClaims claims = claims();
        Prescription prescription = prescription();
        DdiScreeningLog confirmedLog = new DdiScreeningLog();
        confirmedLog.setId(701L);
        confirmedLog.setMatchedRuleId(601L);
        confirmedLog.setConfirmStatus("CONFIRMED_CONTINUE");
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile());
        when(prescriptionMapper.selectByIdAndTenant(1L, 401L)).thenReturn(prescription);
        when(ddiRuleMapper.selectActiveRules(1L)).thenReturn(List.of(highRiskRule()));
        when(ddiScreeningLogMapper.selectByPrescriptionId(1L, 401L)).thenReturn(List.of(confirmedLog));
        when(prescriptionOcrRecordMapper.selectLatestByPrescriptionId(1L, 401L)).thenReturn(ocrRecord("LOW_CONFIDENCE"));
        when(prescriptionAttachmentMapper.selectByPrescriptionId(1L, 401L)).thenReturn(List.of(attachment()));
        when(fileAssetMapper.selectByIdAndTenant(1L, 301L)).thenReturn(fileAsset());
        when(prescriptionItemMapper.selectByPrescriptionId(1L, 401L)).thenReturn(List.of(item("Aspirin"), item("Warfarin")));

        PrescriptionDTO result = adminPrescriptionService.confirmPrescription(claims, 401L, confirmRequest("Aspirin", "Warfarin"));

        verify(prescriptionMapper).updateReviewResult(any(Prescription.class));
        assertThat(result.getStatus()).isEqualTo("PHARMACIST_CONFIRMED");
    }

    @Test
    void shouldConfirmDdiScreening() {
        JwtClaims claims = claims();
        DdiScreeningLog log = new DdiScreeningLog();
        log.setId(701L);
        log.setMatchedRuleId(601L);
        log.setSeverity("HIGH");
        log.setConfirmStatus("CONFIRMED_CONTINUE");
        log.setConfirmationNote("ok");
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile());
        when(ddiScreeningLogMapper.selectByIdAndTenant(1L, 701L)).thenReturn(log);
        when(ddiRuleMapper.selectById(1L, 601L)).thenReturn(highRiskRule());

        DdiScreeningConfirmRequest request = new DdiScreeningConfirmRequest();
        request.setNote("ok");
        PrescriptionDTO.DdiScreeningDTO dto = adminPrescriptionService.confirmScreening(claims, 701L, request);

        assertThat(dto.getSeverity()).isEqualTo("HIGH");
        assertThat(dto.getMatchedRuleId()).isEqualTo(601L);
    }

    @Test
    void shouldRejectPrescription() {
        JwtClaims claims = claims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile());
        when(prescriptionMapper.selectByIdAndTenant(1L, 401L)).thenReturn(prescription());
        when(prescriptionOcrRecordMapper.selectLatestByPrescriptionId(1L, 401L)).thenReturn(ocrRecord("LOW_CONFIDENCE"));
        when(prescriptionAttachmentMapper.selectByPrescriptionId(1L, 401L)).thenReturn(List.of());
        when(prescriptionItemMapper.selectByPrescriptionId(1L, 401L)).thenReturn(List.of());
        when(ddiRuleMapper.selectActiveRules(1L)).thenReturn(List.of());
        when(ddiScreeningLogMapper.selectByPrescriptionId(1L, 401L)).thenReturn(List.of());

        PrescriptionRejectRequest request = new PrescriptionRejectRequest();
        request.setReason("图片不清晰");
        PrescriptionDTO dto = adminPrescriptionService.rejectPrescription(claims, 401L, request);

        assertThat(dto.getStatus()).isEqualTo("REJECTED");
        assertThat(dto.getRejectReason()).isEqualTo("图片不清晰");
    }

    private JwtClaims claims() {
        JwtClaims claims = new JwtClaims();
        claims.setTenantId(1L);
        claims.setUserId(100L);
        claims.setAccountType("PROFESSIONAL");
        claims.setRoles(List.of("pharmacists"));
        return claims;
    }

    private PharmacistProfile profile() {
        PharmacistProfile profile = new PharmacistProfile();
        profile.setId(9001L);
        profile.setTenantId(1L);
        return profile;
    }

    private Prescription prescription() {
        Prescription prescription = new Prescription();
        prescription.setId(401L);
        prescription.setTenantId(1L);
        prescription.setPatientId(201L);
        prescription.setPatientName("Test Patient");
        prescription.setHospitalPatientId("HZY-1001");
        prescription.setStatus("UPLOADED");
        prescription.setSourceType("PHOTO_UPLOAD");
        prescription.setSubmittedByType("PATIENT");
        prescription.setSubmittedById(101L);
        prescription.setCreateTime(LocalDateTime.now());
        return prescription;
    }

    private PrescriptionAttachment attachment() {
        PrescriptionAttachment attachment = new PrescriptionAttachment();
        attachment.setId(501L);
        attachment.setFileAssetId(301L);
        attachment.setAttachmentType("ORIGINAL_IMAGE");
        return attachment;
    }

    private FileAsset fileAsset() {
        FileAsset asset = new FileAsset();
        asset.setId(301L);
        asset.setOriginalFilename("mock.jpg");
        asset.setMimeType("image/jpeg");
        asset.setSizeBytes(1024L);
        asset.setObjectKey("prescriptions/mock.jpg");
        asset.setPublicUrl("download-url");
        return asset;
    }

    private PrescriptionItem item(String drugName) {
        PrescriptionItem item = new PrescriptionItem();
        item.setId((long) drugName.hashCode());
        item.setDrugName(drugName);
        item.setSource("EDITED");
        return item;
    }

    private PrescriptionOcrRecord ocrRecord(String status) {
        PrescriptionOcrRecord record = new PrescriptionOcrRecord();
        record.setId(801L);
        record.setStatus(status);
        record.setConfidence(BigDecimal.ZERO);
        return record;
    }

    private DdiRule highRiskRule() {
        DdiRule rule = new DdiRule();
        rule.setId(601L);
        rule.setDrugAName("Aspirin");
        rule.setDrugBName("Warfarin");
        rule.setSeverity("HIGH");
        rule.setMessage("High risk");
        rule.setRecommendedAction("Confirm first");
        return rule;
    }

    private PrescriptionConfirmRequest confirmRequest(String drugA, String drugB) {
        PrescriptionConfirmRequest request = new PrescriptionConfirmRequest();
        PrescriptionConfirmRequest.ItemInput itemA = new PrescriptionConfirmRequest.ItemInput();
        itemA.setDrugName(drugA);
        itemA.setDurationDays(7);
        PrescriptionConfirmRequest.ItemInput itemB = new PrescriptionConfirmRequest.ItemInput();
        itemB.setDrugName(drugB);
        itemB.setDurationDays(7);
        request.setItems(List.of(itemA, itemB));
        return request;
    }
}
