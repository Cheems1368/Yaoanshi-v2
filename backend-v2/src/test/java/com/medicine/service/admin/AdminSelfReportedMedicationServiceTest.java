package com.medicine.service.admin;

import com.medicine.api.dto.medication.SelfReportedMedicationDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.domain.medication.SelfReportedMedication;
import com.medicine.repository.helporder.HelpOrderEventMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.medication.SelfReportedMedicationMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import com.medicine.service.notification.NotificationDispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSelfReportedMedicationServiceTest {

    @Mock
    private SelfReportedMedicationMapper selfReportedMedicationMapper;
    @Mock
    private HelpOrderMapper helpOrderMapper;
    @Mock
    private HelpOrderEventMapper helpOrderEventMapper;
    @Mock
    private ProfessionalIdentityService professionalIdentityService;
    @Mock
    private NotificationDispatchService notificationDispatchService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> payloadCaptor;

    private AdminSelfReportedMedicationService service;

    private JwtClaims claims;
    private PharmacistProfile pharmacist;

    @BeforeEach
    void setUp() {
        service = new AdminSelfReportedMedicationService(
                selfReportedMedicationMapper,
                helpOrderMapper,
                helpOrderEventMapper,
                professionalIdentityService,
                notificationDispatchService
        );

        claims = new JwtClaims();
        claims.setSubject("pharmacist-1");
        claims.setTenantId(1L);

        pharmacist = new PharmacistProfile();
        pharmacist.setId(10L);
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacist);
    }

    // ---- acknowledge ----

    @Test
    void acknowledge_pendingRecord_changesStatusToActiveAndNotifiesPatient() {
        SelfReportedMedication srm = pendingSrm(1L, 42L, "阿司匹林");
        when(selfReportedMedicationMapper.selectById(1L, 1L)).thenReturn(srm);

        SelfReportedMedicationDTO result = service.acknowledge(claims, 1L);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");

        ArgumentCaptor<String> businessTypeCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationDispatchService).createAndDispatch(
                eq(1L), anyString(),
                eq("PATIENT"), eq(42L),
                businessTypeCaptor.capture(), eq(1L),
                argThat(p -> "阿司匹林".equals(p.get("drugName"))),
                any(LocalDateTime.class));
        assertThat(businessTypeCaptor.getValue()).isEqualTo("SELF_MEDICATION_ACKNOWLEDGED");
    }

    @Test
    void acknowledge_nonPendingRecord_throwsBusinessException() {
        SelfReportedMedication srm = pendingSrm(1L, 42L, "布洛芬");
        srm.setStatus("ACTIVE");
        when(selfReportedMedicationMapper.selectById(1L, 1L)).thenReturn(srm);

        BusinessException ex = catchThrowableOfType(
                () -> service.acknowledge(claims, 1L),
                BusinessException.class);

        assertThat(ex).isNotNull();
        verify(notificationDispatchService, never()).createAndDispatch(
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ---- reject ----

    @Test
    void reject_pendingRecord_changesStatusToStoppedAndNotifiesPatient() {
        SelfReportedMedication srm = pendingSrm(1L, 42L, "布洛芬");
        when(selfReportedMedicationMapper.selectById(1L, 1L)).thenReturn(srm);

        SelfReportedMedicationDTO result = service.reject(claims, 1L, "与处方药有相互作用");

        assertThat(result.getStatus()).isEqualTo("STOPPED");

        ArgumentCaptor<String> businessTypeCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationDispatchService).createAndDispatch(
                eq(1L), anyString(),
                eq("PATIENT"), eq(42L),
                businessTypeCaptor.capture(), eq(1L),
                payloadCaptor.capture(),
                any(LocalDateTime.class));
        assertThat(businessTypeCaptor.getValue()).isEqualTo("SELF_MEDICATION_REJECTED");
        assertThat(payloadCaptor.getValue()).containsEntry("drugName", "布洛芬");
        assertThat(payloadCaptor.getValue()).containsEntry("reason", "与处方药有相互作用");
    }

    @Test
    void reject_withoutReason_notificationPayloadHasNoDrugName() {
        SelfReportedMedication srm = pendingSrm(1L, 42L, "维生素C");
        when(selfReportedMedicationMapper.selectById(1L, 1L)).thenReturn(srm);

        service.reject(claims, 1L, null);

        verify(notificationDispatchService).createAndDispatch(
                any(), any(), any(), any(), any(), any(),
                payloadCaptor.capture(), any());
        assertThat(payloadCaptor.getValue()).containsKey("drugName");
        assertThat(payloadCaptor.getValue()).doesNotContainKey("reason");
    }

    @Test
    void reject_nonPendingRecord_throwsBusinessException() {
        SelfReportedMedication srm = pendingSrm(1L, 42L, "布洛芬");
        srm.setStatus("ACTIVE");
        when(selfReportedMedicationMapper.selectById(1L, 1L)).thenReturn(srm);

        BusinessException ex = catchThrowableOfType(
                () -> service.reject(claims, 1L, "reason"),
                BusinessException.class);

        assertThat(ex).isNotNull();
        verify(notificationDispatchService, never()).createAndDispatch(
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ---- helpers ----

    private SelfReportedMedication pendingSrm(Long tenantId, Long patientId, String drugName) {
        SelfReportedMedication srm = new SelfReportedMedication();
        srm.setId(1L);
        srm.setTenantId(tenantId);
        srm.setPatientId(patientId);
        srm.setDrugName(drugName);
        srm.setStatus("PENDING");
        srm.setOperatorType("PATIENT");
        srm.setOperatorAccountId(patientId);
        return srm;
    }
}
