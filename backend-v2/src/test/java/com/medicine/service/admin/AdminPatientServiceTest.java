package com.medicine.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.identity.AdminPatientDetailDTO;
import com.medicine.api.dto.identity.AdminPatientListItemDTO;
import com.medicine.api.dto.identity.AdminPatientUpdateRequest;
import com.medicine.api.dto.identity.Abcd2InputDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.model.PageResult;
import com.medicine.domain.assessment.Abcd2Assessment;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PatientAdminListRow;
import com.medicine.domain.identity.PatientFamilyBinding;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.repository.assessment.Abcd2AssessmentMapper;
import com.medicine.repository.identity.PharmacistProfileMapper;
import com.medicine.repository.identity.PatientFamilyBindingMapper;
import com.medicine.repository.identity.PatientMapper;
import com.medicine.repository.medication.MedicationPlanMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPatientServiceTest {

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private PatientFamilyBindingMapper patientFamilyBindingMapper;

    @Mock
    private PharmacistProfileMapper pharmacistProfileMapper;

    @Mock
    private Abcd2AssessmentMapper abcd2AssessmentMapper;

    @Mock
    private MedicationPlanMapper medicationPlanMapper;

    @Mock
    private ProfessionalIdentityService professionalIdentityService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AdminPatientService adminPatientService;

    @Test
    void shouldReturnPatientsWithMaskedMobileAndPendingFields() {
        JwtClaims claims = professionalClaims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile(9001L));

        PatientAdminListRow row = new PatientAdminListRow();
        row.setId(3001L);
        row.setName("张明");
        row.setMobile("13800138000");
        row.setHospitalPatientId("HZY-1001");
        row.setBindingStatus("BOUND");
        row.setPrimaryDiseaseName(null);
        row.setAge(null);
        row.setFamilyBindingCount(1);
        row.setResponsiblePharmacistName("药师A");

        when(patientMapper.searchForPharmacist(1L, 9001L, "张", 0, 10)).thenReturn(List.of(row));
        when(patientMapper.countForPharmacist(1L, 9001L, "张")).thenReturn(1L);

        PageResult<AdminPatientListItemDTO> page = adminPatientService.getPatients(claims, "张", 1, 10);

        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getList()).hasSize(1);
        assertThat(page.getList().get(0).getMobileMasked()).isEqualTo("138****8000");
        assertThat(page.getList().get(0).getPendingFields()).contains("年龄", "主要疾病", "药物过敏史", "ABCD2关键信息");
    }

    @Test
    void shouldReturnPatientDetailWithFamilyBindingsAndLatestAbcd2() throws Exception {
        JwtClaims claims = professionalClaims();
        Patient patient = patient(3001L, 9001L);
        patient.setMedicalHistoryJson(objectMapper.writeValueAsString(Map.of(
                "abcd2", Map.of(
                        "bloodPressureHigh", true,
                        "clinicalFeature", "UNILATERAL_WEAKNESS",
                        "symptomDurationMinutes", 45,
                        "diabetes", false
                )
        )));
        Abcd2Assessment assessment = new Abcd2Assessment();
        assessment.setId(7001L);
        assessment.setScore(5);
        assessment.setRiskLevel("MEDIUM");
        assessment.setCalculatedAt(LocalDateTime.of(2026, 3, 6, 10, 0));
        assessment.setRawInputJson(objectMapper.writeValueAsString(Map.of("age60OrOlder", true)));

        PatientFamilyBinding binding = new PatientFamilyBinding();
        binding.setId(4001L);
        binding.setFamilyAccountId(5001L);
        binding.setRelationshipType("child");
        binding.setStatus("ACTIVE");
        binding.setAuthorizedAt(LocalDateTime.of(2026, 3, 6, 9, 30));

        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile(9001L));
        when(patientMapper.selectByIdAndTenant(1L, 3001L)).thenReturn(patient);
        when(pharmacistProfileMapper.selectById(1L, 9001L)).thenReturn(pharmacistProfile(9001L));
        when(patientFamilyBindingMapper.selectActiveByPatientId(1L, 3001L)).thenReturn(List.of(binding));
        when(abcd2AssessmentMapper.selectLatestByPatientId(1L, 3001L)).thenReturn(assessment);
        when(medicationPlanMapper.selectLatestByPatientId(1L, 3001L)).thenReturn(null);

        AdminPatientDetailDTO detail = adminPatientService.getPatientDetail(claims, 3001L);

        assertThat(detail.getFamilyBindings()).hasSize(1);
        assertThat(detail.getLatestAbcd2Assessment().getRiskLevel()).isEqualTo("MEDIUM");
        assertThat(detail.getAbcd2Input().getClinicalFeature()).isEqualTo("UNILATERAL_WEAKNESS");
    }

    @Test
    void shouldCreateAbcd2SnapshotWhenKeyFieldsChange() throws Exception {
        JwtClaims claims = professionalClaims();
        PharmacistProfile pharmacistProfile = pharmacistProfile(9001L);
        Patient patient = patient(3001L, null);
        patient.setMedicalHistoryJson(objectMapper.writeValueAsString(new LinkedHashMap<>()));
        patient.setAge(58);

        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile);
        when(patientMapper.selectByIdAndTenant(1L, 3001L)).thenReturn(patient);
        when(pharmacistProfileMapper.selectById(1L, 9001L)).thenReturn(pharmacistProfile);
        when(patientFamilyBindingMapper.selectActiveByPatientId(1L, 3001L)).thenReturn(List.of());
        when(abcd2AssessmentMapper.selectLatestByPatientId(1L, 3001L)).thenReturn(null);
        when(medicationPlanMapper.selectLatestByPatientId(1L, 3001L)).thenReturn(null);
        doAnswer(invocation -> {
            Abcd2Assessment assessment = invocation.getArgument(0);
            assessment.setId(7002L);
            return 1;
        }).when(abcd2AssessmentMapper).insert(any(Abcd2Assessment.class));

        AdminPatientUpdateRequest request = new AdminPatientUpdateRequest();
        request.setAge(65);
        request.setPrimaryDiseaseName("卒中");
        request.setDrugAllergy("无");
        request.setMedicalHistory(Map.of("smokingHistory", "former"));
        Abcd2InputDTO abcd2Input = new Abcd2InputDTO();
        abcd2Input.setBloodPressureHigh(true);
        abcd2Input.setClinicalFeature("speech_impairment");
        abcd2Input.setSymptomDurationMinutes(30);
        abcd2Input.setDiabetes(true);
        request.setAbcd2Input(abcd2Input);

        AdminPatientDetailDTO detail = adminPatientService.updatePatient(claims, 3001L, request);

        verify(patientMapper).updateForPharmacist(any(Patient.class));
        verify(abcd2AssessmentMapper).insert(any(Abcd2Assessment.class));
        assertThat(detail.getResponsiblePharmacistId()).isEqualTo(9001L);
        assertThat(detail.getAbcd2Input().getDiabetes()).isTrue();
    }

    @Test
    void shouldNotAllowOtherPharmacistToEditPatient() {
        JwtClaims claims = professionalClaims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile(9001L));
        when(patientMapper.selectByIdAndTenant(1L, 3001L)).thenReturn(patient(3001L, 9002L));

        assertThatThrownBy(() -> adminPatientService.updatePatient(claims, 3001L, new AdminPatientUpdateRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("PATIENT_MANAGEMENT_PERMISSION_DENIED");

        verify(patientMapper, never()).updateForPharmacist(any(Patient.class));
    }

    private JwtClaims professionalClaims() {
        JwtClaims claims = new JwtClaims();
        claims.setAccountType("PROFESSIONAL");
        claims.setTenantId(1L);
        claims.setUserId(100L);
        claims.setRoles(List.of("pharmacists"));
        return claims;
    }

    private PharmacistProfile pharmacistProfile(Long id) {
        PharmacistProfile profile = new PharmacistProfile();
        profile.setId(id);
        profile.setTenantId(1L);
        profile.setRealName("药师A");
        return profile;
    }

    private Patient patient(Long id, Long boundByPharmacistId) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setTenantId(1L);
        patient.setName("张明");
        patient.setMobile("13800138000");
        patient.setHospitalPatientId("HZY-1001");
        patient.setBindingStatus("BOUND");
        patient.setBoundByPharmacistId(boundByPharmacistId);
        patient.setBirthDate(LocalDate.of(1960, 1, 1));
        patient.setAge(66);
        patient.setPrimaryDiseaseName("高血压");
        patient.setDrugAllergy("无");
        return patient;
    }
}
