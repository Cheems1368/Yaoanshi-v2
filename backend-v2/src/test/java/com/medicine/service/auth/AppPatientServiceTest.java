package com.medicine.service.auth;

import com.medicine.api.dto.auth.BindPatientRequest;
import com.medicine.api.dto.identity.AppFamilyBindingDTO;
import com.medicine.api.dto.identity.AppPatientSummaryDTO;
import com.medicine.api.dto.identity.CreateFamilyBindingRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PatientFamilyBinding;
import com.medicine.repository.identity.AppAccountMapper;
import com.medicine.repository.identity.PatientFamilyBindingMapper;
import com.medicine.repository.identity.PatientMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppPatientServiceTest {

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private AppAccountMapper appAccountMapper;

    @Mock
    private PatientFamilyBindingMapper patientFamilyBindingMapper;

    @InjectMocks
    private AppPatientService appPatientService;

    @Test
    void shouldRejectFamilyAccountListingPatientFamilyBindings() {
        AppAccount family = appAccount(2001L, "13900139000");
        family.setAccountType("FAMILY");
        when(appAccountMapper.selectActiveByMobile(1L, "13900139000")).thenReturn(family);

        assertThatThrownBy(() -> appPatientService.getFamilyBindings("13900139000"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("BAD_REQUEST");
        verify(patientFamilyBindingMapper, never()).selectActiveByFamilyAccountId(1L, 2001L);
    }

    @Test
    void shouldRejectAnExistingPatientAccountAsAFamilyAccount() {
        AppAccount owner = appAccount(1001L, "13800138000");
        owner.setAccountType("PATIENT");
        AppAccount existingPatient = appAccount(2001L, "13900139000");
        existingPatient.setAccountType("PATIENT");
        Patient patient = new Patient();
        patient.setId(3001L);
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(owner);
        when(patientMapper.selectByAppAccountId(1L, 1001L)).thenReturn(patient);
        when(patientFamilyBindingMapper.countActiveByPatientId(1L, 3001L)).thenReturn(0);
        when(appAccountMapper.selectActiveByMobile(1L, "13900139000")).thenReturn(existingPatient);
        CreateFamilyBindingRequest request = new CreateFamilyBindingRequest();
        request.setFamilyMobile("13900139000");
        request.setFamilyDisplayName("家属");
        request.setRelationshipType("child");

        assertThatThrownBy(() -> appPatientService.createFamilyBinding("13800138000", request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("PATIENT_ACCOUNT_CANNOT_BE_FAMILY");
        verify(appAccountMapper, never()).updateLoginInfo(any());
        verify(patientFamilyBindingMapper, never()).insert(any());
    }

    @Test
    void shouldRejectFamilyAccountAlreadyBoundToAnotherPatient() {
        AppAccount owner = appAccount(1001L, "13800138000");
        owner.setAccountType("PATIENT");
        AppAccount family = appAccount(2001L, "13900139000");
        family.setAccountType("FAMILY");
        Patient patient = new Patient();
        patient.setId(3001L);
        PatientFamilyBinding other = new PatientFamilyBinding();
        other.setPatientId(3999L);
        other.setFamilyAccountId(2001L);
        other.setStatus("ACTIVE");
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(owner);
        when(patientMapper.selectByAppAccountId(1L, 1001L)).thenReturn(patient);
        when(patientFamilyBindingMapper.countActiveByPatientId(1L, 3001L)).thenReturn(0);
        when(appAccountMapper.selectActiveByMobile(1L, "13900139000")).thenReturn(family);
        when(patientFamilyBindingMapper.selectActiveByFamilyAccountId(1L, 2001L)).thenReturn(List.of(other));
        CreateFamilyBindingRequest request = new CreateFamilyBindingRequest();
        request.setFamilyMobile("13900139000");
        request.setFamilyDisplayName("家属");
        request.setRelationshipType("child");

        assertThatThrownBy(() -> appPatientService.createFamilyBinding("13800138000", request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("FAMILY_ACCOUNT_ALREADY_BOUND");
        verify(patientFamilyBindingMapper, never()).insert(any());
        verify(patientMapper).lockByIdAndTenant(1L, 3001L);
    }

    @Test
    void shouldMapConcurrentFamilyUniquenessConflictToBusinessError() {
        AppAccount owner = appAccount(1001L, "13800138000");
        owner.setAccountType("PATIENT");
        AppAccount family = appAccount(2001L, "13900139000");
        family.setAccountType("FAMILY");
        Patient patient = new Patient();
        patient.setId(3001L);
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(owner);
        when(patientMapper.selectByAppAccountId(1L, 1001L)).thenReturn(patient);
        when(patientFamilyBindingMapper.countActiveByPatientId(1L, 3001L)).thenReturn(1);
        when(appAccountMapper.selectActiveByMobile(1L, "13900139000")).thenReturn(family);
        when(patientFamilyBindingMapper.selectActiveByFamilyAccountId(1L, 2001L)).thenReturn(List.of());
        when(patientFamilyBindingMapper.selectActiveBinding(1L, 3001L, 2001L)).thenReturn(null);
        doThrow(new DataIntegrityViolationException("duplicate family account"))
                .when(patientFamilyBindingMapper).insert(any(PatientFamilyBinding.class));
        CreateFamilyBindingRequest request = new CreateFamilyBindingRequest();
        request.setFamilyMobile("13900139000");
        request.setFamilyDisplayName("家属");
        request.setRelationshipType("child");

        assertThatThrownBy(() -> appPatientService.createFamilyBinding("13800138000", request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("FAMILY_ACCOUNT_ALREADY_BOUND");
    }

    @Test
    void shouldInsertPatientBindingWhenNoExistingRecord() {
        AppAccount appAccount = appAccount(1001L, "13800138000");
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(appAccount);
        when(patientMapper.selectByAppAccountId(1L, 1001L)).thenReturn(null);
        when(patientMapper.selectByHospitalPatientId(1L, "HZY-1001")).thenReturn(null);
        when(patientMapper.selectByMobileAndHospitalPatientId(1L, "13800138000", "HZY-1001")).thenReturn(null);
        doAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            patient.setId(3001L);
            return 1;
        }).when(patientMapper).insert(any(Patient.class));

        AppPatientSummaryDTO result = appPatientService.bindPatient("13800138000", bindRequest("HZY-1001"));

        assertThat(result.getId()).isEqualTo(3001L);
        assertThat(result.isBound()).isFalse();
        assertThat(result.getBindingStatus()).isEqualTo("PENDING");
        assertThat(result.getBoundAt()).isNull();
        verify(patientMapper).insert(any(Patient.class));
    }

    @Test
    void shouldDenyMedicalAccessUntilPharmacistConfirmsBinding() {
        AppAccount account = appAccount(1001L, "13800138000");
        Patient patient = new Patient();
        patient.setId(3001L);
        patient.setBindingStatus("PENDING");
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(account);
        when(patientMapper.selectByAppAccountId(1L, 1001L)).thenReturn(patient);

        assertThatThrownBy(() -> appPatientService.requireAccessContext("13800138000"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("PATIENT_BIND_REQUIRES_PHARMACIST_CONFIRM");
    }

    @Test
    void shouldPreserveExistingConfirmedBindingOnResubmission() {
        AppAccount account = appAccount(1001L, "13800138000");
        Patient patient = new Patient();
        patient.setId(3001L);
        patient.setAppAccountId(1001L);
        patient.setHospitalPatientId("HZY-1001");
        patient.setMobile("13800138000");
        patient.setBindingStatus("BOUND");
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 3, 6, 9, 30);
        patient.setBoundAt(confirmedAt);
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(account);
        when(patientMapper.selectByAppAccountId(1L, 1001L)).thenReturn(patient);
        when(patientMapper.selectByHospitalPatientId(1L, "HZY-1001")).thenReturn(patient);
        when(patientMapper.selectByMobileAndHospitalPatientId(1L, "13800138000", "HZY-1001")).thenReturn(patient);
        org.mockito.Mockito.lenient().when(patientMapper.lockByIdAndTenant(1L, 3001L)).thenReturn(patient);

        AppPatientSummaryDTO result = appPatientService.bindPatient("13800138000", bindRequest("HZY-1001"));

        assertThat(result.isBound()).isTrue();
        assertThat(patient.getBoundAt()).isEqualTo(confirmedAt);
    }

    @Test
    void shouldRejectBindingWhenHospitalIdAlreadyOwnedByAnotherAccount() {
        AppAccount currentAccount = appAccount(1001L, "13800138000");
        Patient duplicatePatient = new Patient();
        duplicatePatient.setId(3002L);
        duplicatePatient.setAppAccountId(2002L);
        duplicatePatient.setHospitalPatientId("HZY-1001");
        duplicatePatient.setMobile("13900139000");

        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(currentAccount);
        when(patientMapper.selectByAppAccountId(1L, 1001L)).thenReturn(null);
        when(patientMapper.selectByHospitalPatientId(1L, "HZY-1001")).thenReturn(duplicatePatient);

        assertThatThrownBy(() -> appPatientService.bindPatient("13800138000", bindRequest("HZY-1001")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("PATIENT_BIND_REQUIRES_PHARMACIST_CONFIRM");

        verify(patientMapper, never()).insert(any(Patient.class));
    }

    @Test
    void shouldReadCurrentPatientAndFamilyBindingsFromPersistence() {
        AppAccount appAccount = appAccount(1001L, "13800138000");
        appAccount.setAccountType("PATIENT");
        Patient patient = new Patient();
        patient.setId(3001L);
        patient.setAppAccountId(1001L);
        patient.setHospitalPatientId("HZY-1001");
        patient.setMobile("13800138000");
        patient.setName("Test Patient");
        patient.setBindingStatus("BOUND");
        patient.setBoundAt(LocalDateTime.of(2026, 3, 6, 9, 30));

        PatientFamilyBinding binding = new PatientFamilyBinding();
        binding.setId(4001L);
        binding.setPatientId(3001L);
        binding.setFamilyAccountId(5001L);
        binding.setRelationshipType("spouse");
        binding.setStatus("ACTIVE");
        binding.setAuthorizedAt(LocalDateTime.of(2026, 3, 6, 10, 0));

        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(appAccount);
        when(patientMapper.selectByAppAccountId(1L, 1001L)).thenReturn(patient);
        when(patientFamilyBindingMapper.selectActiveByPatientId(1L, 3001L)).thenReturn(List.of(binding));

        AppPatientSummaryDTO currentPatient = appPatientService.getCurrentPatient("13800138000");
        List<AppFamilyBindingDTO> familyBindings = appPatientService.getFamilyBindings("13800138000");

        assertThat(currentPatient.getHospitalPatientId()).isEqualTo("HZY-1001");
        assertThat(currentPatient.isBound()).isEqualTo(true);
        assertThat(familyBindings).hasSize(1);
        assertThat(familyBindings.get(0).getFamilyAccountId()).isEqualTo(5001L);
    }

    private AppAccount appAccount(Long id, String mobile) {
        AppAccount appAccount = new AppAccount();
        appAccount.setId(id);
        appAccount.setMobile(mobile);
        return appAccount;
    }

    private BindPatientRequest bindRequest(String hospitalPatientId) {
        BindPatientRequest request = new BindPatientRequest();
        request.setMobile("13800138000");
        request.setHospitalPatientId(hospitalPatientId);
        request.setName("Test Patient");
        return request;
    }
}
