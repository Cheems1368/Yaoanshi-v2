package com.medicine.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.medication.DoseTaskDTO;
import com.medicine.api.dto.ddi.AdminDdiScreenResult;
import com.medicine.api.dto.ddi.AdminDdiRiskCardDTO;
import com.medicine.api.dto.medication.PlanActivationRequest;
import com.medicine.api.dto.medication.MedicationPlanDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.medication.MedicationPlan;
import com.medicine.domain.medication.MedicationPlanItem;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.domain.medication.MedicationDoseTask;
import com.medicine.repository.medication.MedicationDoseTaskMapper;
import com.medicine.repository.medication.MedicationPlanEventMapper;
import com.medicine.repository.medication.MedicationPlanItemMapper;
import com.medicine.repository.medication.MedicationPlanMapper;
import com.medicine.repository.identity.PatientMapper;
import com.medicine.service.app.AppDdiScreeningService;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AdminMedicationPlanServiceTest {

    @Mock
    private MedicationPlanMapper medicationPlanMapper;

    @Mock
    private MedicationPlanItemMapper medicationPlanItemMapper;

    @Mock
    private MedicationPlanEventMapper medicationPlanEventMapper;

    @Mock
    private MedicationDoseTaskMapper medicationDoseTaskMapper;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private AppDdiScreeningService appDdiScreeningService;

    @Mock
    private AdherenceService adherenceService;

    @Mock
    private ProfessionalIdentityService professionalIdentityService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AdminMedicationPlanService adminMedicationPlanService;

    @Test
    void shouldMapDoseTasksWithinExplicitDateRange() {
        JwtClaims claims = pharmacistClaims();
        stubReadablePlan(claims);
        when(medicationDoseTaskMapper.selectByPlanIdAndDateRange(1L, 300L, LocalDate.parse("2026-03-01"), LocalDate.parse("2026-03-02")))
                .thenReturn(List.of(doseTask()));

        List<DoseTaskDTO> result = adminMedicationPlanService.getPlanTasks(claims, 300L, "2026-03-01", "2026-03-02");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDrugName()).isEqualTo("Aspirin");
        assertThat(result.get(0).getScheduledAt()).isEqualTo("2026-03-01T08:00:00Z");
    }

    @Test
    void shouldBuildAdherenceReportFromDoseTaskStatus() {
        JwtClaims claims = pharmacistClaims();
        stubReadablePlan(claims);
        when(adherenceService.summarizeRange(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(201L),
                org.mockito.ArgumentMatchers.eq(300L), org.mockito.ArgumentMatchers.eq(LocalDate.parse("2026-03-01")),
                org.mockito.ArgumentMatchers.eq(LocalDate.parse("2026-03-07")), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(new AdherenceService.Summary(LocalDate.parse("2026-03-01"), LocalDate.parse("2026-03-07"),
                        LocalDateTime.of(2026, 3, 7, 12, 0), 3, 2, 1, 1, 0));

        String report = adminMedicationPlanService.buildAdherenceReport(claims, 300L, "2026-03-01", "2026-03-07");

        assertThat(report).contains("planId,from,to,totalTasks,completedTasks,adherenceRate");
        assertThat(report).contains("300,2026-03-01,2026-03-07,3,2,66.67");
        assertThat(report).contains("onTimeTakenTasks,earlyOver60Minutes,selfReportedOnTimeRate");
        assertThat(report).endsWith(",1,1,33.33");
    }

    @Test
    void shouldRejectInvalidDateFormatWhenQueryingDoseTasks() {
        JwtClaims claims = pharmacistClaims();
        stubReadablePlan(claims);

        assertThatThrownBy(() -> adminMedicationPlanService.getPlanTasks(claims, 300L, "bad-date", "2026-03-07"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldReplacePreviousActivePlansInsteadOfLeavingThemActive() throws Exception {
        Update update = MedicationPlanMapper.class.getMethod(
                "replaceOtherActivePlans", com.medicine.domain.medication.MedicationPlan.class
        ).getAnnotation(Update.class);

        assertThat(String.join(" ", update.value())).contains("SET status = 'REPLACED'");
    }

    @Test
    void activationUsesShanghaiClockTimeAndIsIdempotent() {
        JwtClaims claims = pharmacistClaims();
        MedicationPlan plan = plan();
        when(medicationPlanMapper.selectById(1L, 300L)).thenReturn(plan);
        when(medicationPlanMapper.selectByIdForUpdate(1L, 300L)).thenReturn(plan);
        when(patientMapper.lockByIdAndTenant(1L, 201L)).thenReturn(patient());
        when(patientMapper.selectByIdAndTenant(1L, 201L)).thenReturn(patient());
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile());
        MedicationPlanItem item = new MedicationPlanItem();
        item.setId(400L);
        item.setDrugName("Aspirin");
        item.setDosage("1 tablet");
        item.setFrequencyCode("QD");
        item.setTimePointsJson("[\"08:00\"]");
        when(medicationPlanItemMapper.selectByPlanId(1L, 300L)).thenReturn(List.of(item));
        AdminDdiScreenResult screening = new AdminDdiScreenResult();
        screening.setAlerts(List.of());
        when(appDdiScreeningService.screenPlanDrugsDetailed(1L, 201L, List.of("Aspirin"))).thenReturn(screening);

        adminMedicationPlanService.activatePlan(claims, 300L, new PlanActivationRequest());
        ArgumentCaptor<MedicationDoseTask> captor = ArgumentCaptor.forClass(MedicationDoseTask.class);
        verify(medicationDoseTaskMapper, org.mockito.Mockito.atLeastOnce()).insert(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(task ->
                assertThat(task.getScheduledAt().getHour()).isEqualTo(0));

        adminMedicationPlanService.activatePlan(claims, 300L, new PlanActivationRequest());
        verify(medicationPlanMapper, org.mockito.Mockito.times(1)).replaceOtherActivePlans(plan);
    }

    @Test
    void concurrentActivationSeesCommittedStatusAfterPatientLock() {
        JwtClaims claims = pharmacistClaims();
        MedicationPlan staleDraft = plan();
        MedicationPlan committedActive = plan();
        committedActive.setStatus("ACTIVE");
        when(medicationPlanMapper.selectById(1L, 300L)).thenReturn(staleDraft);
        when(medicationPlanMapper.selectByIdForUpdate(1L, 300L)).thenReturn(committedActive);
        when(patientMapper.lockByIdAndTenant(1L, 201L)).thenReturn(patient());
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile());

        MedicationPlanDTO result = adminMedicationPlanService.activatePlan(claims, 300L, new PlanActivationRequest());

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(medicationPlanMapper, never()).replaceOtherActivePlans(org.mockito.ArgumentMatchers.any());
        verify(medicationDoseTaskMapper, never()).insert(org.mockito.ArgumentMatchers.any());
        verify(medicationPlanEventMapper, never()).insert(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void activationRejectsMissingDdiConfirmationWithoutChangingPlan() {
        JwtClaims claims = pharmacistClaims();
        MedicationPlan plan = plan();
        when(medicationPlanMapper.selectById(1L, 300L)).thenReturn(plan);
        when(medicationPlanMapper.selectByIdForUpdate(1L, 300L)).thenReturn(plan);
        when(patientMapper.lockByIdAndTenant(1L, 201L)).thenReturn(patient());
        when(patientMapper.selectByIdAndTenant(1L, 201L)).thenReturn(patient());
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile());
        MedicationPlanItem item = new MedicationPlanItem();
        item.setDrugName("Aspirin");
        when(medicationPlanItemMapper.selectByPlanId(1L, 300L)).thenReturn(List.of(item));
        AdminDdiRiskCardDTO alert = new AdminDdiRiskCardDTO();
        alert.setCardKey("risk-1");
        AdminDdiScreenResult screening = new AdminDdiScreenResult();
        screening.setAlerts(List.of(alert));
        when(appDdiScreeningService.screenPlanDrugsDetailed(1L, 201L, List.of("Aspirin"))).thenReturn(screening);

        assertThatThrownBy(() -> adminMedicationPlanService.activatePlan(claims, 300L, new PlanActivationRequest()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("DDI_CONFIRM_REQUIRED");
        verify(medicationPlanMapper, never()).update(plan);
        verify(medicationDoseTaskMapper, never()).insert(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void otherPharmacistCannotReadPatientPlan() {
        JwtClaims claims = pharmacistClaims();
        when(medicationPlanMapper.selectById(1L, 300L)).thenReturn(plan());
        Patient patient = patient();
        patient.setBoundByPharmacistId(999L);
        when(patientMapper.selectByIdAndTenant(1L, 201L)).thenReturn(patient);
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile());

        assertThatThrownBy(() -> adminMedicationPlanService.getPlan(claims, 300L))
                .isInstanceOf(BusinessException.class);
        verify(medicationPlanItemMapper, never()).selectByPlanId(1L, 300L);
    }

    @Test
    void editingDraftKeepsItsVersionAndDoesNotActivateIt() {
        JwtClaims claims = pharmacistClaims();
        MedicationPlan draft = plan();
        draft.setVersionNo(3);
        draft.setPlanName("Original");
        draft.setSourceType("MANUAL");
        draft.setCreatedByPharmacistId(101L);
        when(medicationPlanMapper.selectById(1L, 300L)).thenReturn(draft);
        when(medicationPlanMapper.selectByIdForUpdate(1L, 300L)).thenReturn(draft);
        when(patientMapper.lockByIdAndTenant(1L, 201L)).thenReturn(patient());
        when(patientMapper.selectByIdAndTenant(1L, 201L)).thenReturn(patient());
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile());
        MedicationPlanDTO request = new MedicationPlanDTO();
        request.setPlanName("Edited");
        MedicationPlanDTO.ItemDTO item = new MedicationPlanDTO.ItemDTO();
        item.setDrugName("Aspirin");
        item.setDosage("1 tablet");
        item.setFrequencyCode("QD");
        item.setTimePoints(List.of("08:00"));
        request.setItems(List.of(item));

        MedicationPlanDTO result = adminMedicationPlanService.updatePlan(claims, 300L, request);

        assertThat(result.getVersionNo()).isEqualTo(3);
        assertThat(result.getStatus()).isEqualTo("DRAFT");
        verify(medicationPlanMapper, never()).insert(org.mockito.ArgumentMatchers.any());
        verify(medicationPlanItemMapper).softDeleteByPlanId(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(300L), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class));
        verify(medicationPlanItemMapper).insert(org.mockito.ArgumentMatchers.any());
        verify(medicationDoseTaskMapper, never()).insert(org.mockito.ArgumentMatchers.any());
    }

    private void stubReadablePlan(JwtClaims claims) {
        when(medicationPlanMapper.selectById(1L, 300L)).thenReturn(plan());
        when(patientMapper.selectByIdAndTenant(1L, 201L)).thenReturn(patient());
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile());
    }

    private MedicationPlan plan() {
        MedicationPlan plan = new MedicationPlan();
        plan.setId(300L);
        plan.setPatientId(201L);
        plan.setTenantId(1L);
        plan.setStatus("DRAFT");
        return plan;
    }

    private Patient patient() {
        Patient patient = new Patient();
        patient.setId(201L);
        patient.setTenantId(1L);
        patient.setBoundByPharmacistId(101L);
        return patient;
    }

    private JwtClaims pharmacistClaims() {
        JwtClaims claims = new JwtClaims();
        claims.setTenantId(1L);
        claims.setRoles(List.of("pharmacists"));
        claims.setAccountType("PROFESSIONAL");
        return claims;
    }

    private PharmacistProfile pharmacistProfile() {
        PharmacistProfile profile = new PharmacistProfile();
        profile.setId(101L);
        profile.setTenantId(1L);
        return profile;
    }

    private MedicationDoseTask doseTask() {
        MedicationDoseTask task = new MedicationDoseTask();
        task.setId(1L);
        task.setPatientId(201L);
        task.setPlanId(300L);
        task.setPlanItemId(400L);
        task.setTaskDate(LocalDate.parse("2026-03-01"));
        task.setScheduledAt(LocalDateTime.of(2026, 3, 1, 8, 0));
        task.setStatus("PENDING");
        task.setNotificationStatus("PENDING");
        task.setDrugName("Aspirin");
        task.setDosage("1 tablet");
        task.setFrequencyCode("BID");
        return task;
    }

    private MedicationDoseTask doseTaskWithStatus(String status) {
        MedicationDoseTask task = doseTask();
        task.setStatus(status);
        return task;
    }
}
