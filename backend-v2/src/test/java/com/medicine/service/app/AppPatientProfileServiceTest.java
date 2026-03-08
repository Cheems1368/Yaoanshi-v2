package com.medicine.service.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.identity.AppPatientHealthProfileDTO;
import com.medicine.api.dto.identity.AppPatientHealthProfileUpdateRequest;
import com.medicine.api.dto.identity.AppPatientProfileDTO;
import com.medicine.api.dto.identity.AppPatientProfileUpdateRequest;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PatientHealthProfile;
import com.medicine.domain.identity.PatientProfile;
import com.medicine.repository.identity.PatientHealthProfileMapper;
import com.medicine.repository.identity.PatientMapper;
import com.medicine.repository.identity.PatientProfileMapper;
import com.medicine.service.auth.AppPatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppPatientProfileServiceTest {

    @Mock
    private AppPatientService appPatientService;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private PatientProfileMapper patientProfileMapper;

    @Mock
    private PatientHealthProfileMapper patientHealthProfileMapper;

    private AppPatientProfileService appPatientProfileService;

    @BeforeEach
    void setUp() {
        appPatientProfileService = new AppPatientProfileService(
                appPatientService,
                patientMapper,
                patientProfileMapper,
                patientHealthProfileMapper,
                new ObjectMapper()
        );
    }

    @Test
    void shouldReadMergedPatientProfileFromPersistence() {
        Patient patient = patient();
        PatientProfile profile = new PatientProfile();
        profile.setPatientId(3001L);
        profile.setContactMobile("13900001111");
        profile.setIdType("院内患者编号");
        profile.setHeightCm(172);
        profile.setWeightKg(65);
        profile.setAllergies("青霉素过敏");
        profile.setEmergencyContactName("李女士");
        profile.setEmergencyContactPhone("13800002222");
        profile.setEmergencyContactRelationship("配偶");
        profile.setRealNameVerified(true);
        profile.setRealNameVerifiedTime(LocalDateTime.of(2026, 3, 7, 8, 0));
        profile.setCreateTime(LocalDateTime.of(2026, 3, 7, 8, 0));
        profile.setUpdateTime(LocalDateTime.of(2026, 3, 7, 9, 0));

        when(appPatientService.requireAccessContext("13800138000"))
                .thenReturn(new AppPatientService.AccessContext(appAccount(), patient, "PATIENT"));
        when(patientProfileMapper.selectByPatientId(1L, 3001L)).thenReturn(profile);

        AppPatientProfileDTO dto = appPatientProfileService.getProfile("13800138000");

        assertThat(dto.getPatientId()).isEqualTo(3001L);
        assertThat(dto.getPhone()).isEqualTo("13900001111");
        assertThat(dto.getIdType()).isEqualTo("院内患者编号");
        assertThat(dto.getEmergencyContact().getRelationship()).isEqualTo("配偶");
        assertThat(dto.getHospitalPatientId()).isNull();
        assertThat(dto.getIdCard()).isNull();
        assertThat(dto.getRealNameVerified()).isTrue();
    }

    @Test
    void shouldPersistPatientProfileAndMaskIdentifier() {
        Patient patient = patient();
        when(appPatientService.requireAccessContext("13800138000"))
                .thenReturn(new AppPatientService.AccessContext(appAccount(), patient, "PATIENT"));
        when(patientProfileMapper.selectByPatientId(1L, 3001L)).thenReturn(null);

        AppPatientProfileUpdateRequest request = new AppPatientProfileUpdateRequest();
        request.setName("测试患者");
        request.setPhone("13900001111");
        request.setAge(32);
        request.setGender("女");
        request.setIdCard("330123199001011234");
        request.setIdType("身份证");
        request.setAllergies("花粉过敏");
        request.setRealNameVerified(true);
        request.setRealNameVerifiedTime("2026-03-07T08:00:00Z");
        AppPatientProfileUpdateRequest.EmergencyContactRequest emergencyContact = new AppPatientProfileUpdateRequest.EmergencyContactRequest();
        emergencyContact.setName("家属");
        emergencyContact.setPhone("13700003333");
        emergencyContact.setRelationship("父母");
        request.setEmergencyContact(emergencyContact);

        AppPatientProfileDTO dto = appPatientProfileService.updateProfile("13800138000", request);

        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientMapper).updateAppProfile(patientCaptor.capture());
        assertThat(patientCaptor.getValue().getIdCardMasked()).isNull();

        ArgumentCaptor<PatientProfile> profileCaptor = ArgumentCaptor.forClass(PatientProfile.class);
        verify(patientProfileMapper).upsert(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getContactMobile()).isEqualTo("13900001111");
        assertThat(profileCaptor.getValue().getEmergencyContactRelationship()).isEqualTo("父母");
        assertThat(profileCaptor.getValue().getIdentityMaterialHash()).hasSize(64);
        assertThat(dto.getPhone()).isEqualTo("13900001111");
        assertThat(dto.getIdentityMaterialArchived()).isTrue();
        assertThat(dto.getRealNameVerified()).isTrue();
    }

    @Test
    void shouldNotTreatLegacyMaskedIdentifierAsArchivedMaterial() {
        Patient patient = patient();
        when(appPatientService.requireAccessContext("13800138000"))
                .thenReturn(new AppPatientService.AccessContext(appAccount(), patient, "PATIENT"));
        when(patientProfileMapper.selectByPatientId(1L, 3001L)).thenReturn(null);

        AppPatientProfileDTO dto = appPatientProfileService.getProfile("13800138000");

        assertThat(dto.getIdentityMaterialArchived()).isFalse();
    }

    @Test
    void shouldPersistHealthQuestionnaireAndSyncPatientSnapshot() {
        Patient patient = patient();
        when(appPatientService.requireAccessContext("13800138000"))
                .thenReturn(new AppPatientService.AccessContext(appAccount(), patient, "PATIENT"));
        when(patientHealthProfileMapper.selectByPatientId(1L, 3001L)).thenReturn(null);
        when(patientProfileMapper.selectByPatientId(1L, 3001L)).thenReturn(null);

        AppPatientHealthProfileUpdateRequest request = new AppPatientHealthProfileUpdateRequest();
        AppPatientHealthProfileDTO.Layer1DTO layer1 = new AppPatientHealthProfileDTO.Layer1DTO();
        layer1.setAllergy("青霉素过敏");
        layer1.setDiagnosis("高血压");
        AppPatientHealthProfileDTO.Layer2DTO layer2 = new AppPatientHealthProfileDTO.Layer2DTO();
        layer2.setMedicalHistory("既往脑梗");
        layer2.setCurrentMedication("阿司匹林");
        AppPatientHealthProfileDTO.Layer3DTO layer3 = new AppPatientHealthProfileDTO.Layer3DTO();
        layer3.setFamilyHistory("父亲高血压");
        request.setLayer1(layer1);
        request.setLayer2(layer2);
        request.setLayer3(layer3);

        AppPatientHealthProfileDTO dto = appPatientProfileService.updateHealthProfile("13800138000", request);

        ArgumentCaptor<PatientHealthProfile> healthCaptor = ArgumentCaptor.forClass(PatientHealthProfile.class);
        verify(patientHealthProfileMapper).upsert(healthCaptor.capture());
        assertThat(healthCaptor.getValue().getLayer1Diagnosis()).isEqualTo("高血压");

        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientMapper).updateAppHealthSummary(patientCaptor.capture());
        assertThat(patientCaptor.getValue().getPrimaryDiseaseName()).isEqualTo("高血压");
        assertThat(patientCaptor.getValue().getMedicalHistoryJson()).contains("阿司匹林");

        verify(patientProfileMapper).upsert(any(PatientProfile.class));
        assertThat(dto.getLayer1().getAllergy()).isEqualTo("青霉素过敏");
        assertThat(dto.getSubmitTime()).isNotNull();
    }

    private AppAccount appAccount() {
        AppAccount appAccount = new AppAccount();
        appAccount.setId(1001L);
        appAccount.setTenantId(1L);
        return appAccount;
    }

    private Patient patient() {
        Patient patient = new Patient();
        patient.setId(3001L);
        patient.setTenantId(1L);
        patient.setHospitalPatientId("HZY-1001");
        patient.setMobile("13800138000");
        patient.setName("测试患者");
        patient.setGender("男");
        patient.setAge(30);
        patient.setIdCardMasked("330***********1234");
        patient.setCreateTime(LocalDateTime.of(2026, 3, 7, 7, 30));
        patient.setUpdateTime(LocalDateTime.of(2026, 3, 7, 7, 30));
        return patient;
    }
}
