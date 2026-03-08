package com.medicine.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.medication.DoseTaskDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.domain.medication.MedicationDoseTask;
import com.medicine.repository.medication.MedicationDoseTaskMapper;
import com.medicine.repository.medication.MedicationPlanEventMapper;
import com.medicine.repository.medication.MedicationPlanItemMapper;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private ProfessionalIdentityService professionalIdentityService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AdminMedicationPlanService adminMedicationPlanService;

    @Test
    void shouldMapDoseTasksWithinExplicitDateRange() {
        JwtClaims claims = pharmacistClaims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile());
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
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile());
        when(medicationDoseTaskMapper.selectByPlanIdAndDateRange(1L, 300L, LocalDate.parse("2026-03-01"), LocalDate.parse("2026-03-07")))
                .thenReturn(List.of(doseTaskWithStatus("COMPLETED"), doseTaskWithStatus("MISSED"), doseTaskWithStatus("COMPLETED")));

        String report = adminMedicationPlanService.buildAdherenceReport(claims, 300L, "2026-03-01", "2026-03-07");

        assertThat(report).contains("planId,from,to,totalTasks,completedTasks,adherenceRate");
        assertThat(report).contains("300,2026-03-01,2026-03-07,3,2,66.67");
    }

    @Test
    void shouldRejectInvalidDateFormatWhenQueryingDoseTasks() {
        JwtClaims claims = pharmacistClaims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile());

        assertThatThrownBy(() -> adminMedicationPlanService.getPlanTasks(claims, 300L, "bad-date", "2026-03-07"))
                .isInstanceOf(RuntimeException.class);
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
