package com.medicine.service.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.medication.CheckinCorrectRequest;
import com.medicine.api.dto.medication.CheckinRecordDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.medication.MedicationCheckinRecord;
import com.medicine.domain.medication.MedicationDoseTask;
import com.medicine.repository.medication.MedicationCheckinRecordMapper;
import com.medicine.repository.medication.MedicationDoseTaskMapper;
import com.medicine.repository.medication.MedicationPlanEventMapper;
import com.medicine.repository.medication.MedicationPlanItemMapper;
import com.medicine.repository.medication.MedicationPlanMapper;
import com.medicine.service.auth.AppPatientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppMedicationServiceTest {

    @Mock
    private MedicationPlanMapper medicationPlanMapper;

    @Mock
    private MedicationPlanItemMapper medicationPlanItemMapper;

    @Mock
    private MedicationPlanEventMapper medicationPlanEventMapper;

    @Mock
    private MedicationDoseTaskMapper medicationDoseTaskMapper;

    @Mock
    private MedicationCheckinRecordMapper medicationCheckinRecordMapper;

    @Mock
    private AppPatientService appPatientService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AppMedicationService appMedicationService;

    @Test
    void shouldRejectCorrectionWhenEditWindowExpired() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(medicationCheckinRecordMapper.selectById(1L, 701L)).thenReturn(checkinRecord(701L, 601L, true));
        when(medicationDoseTaskMapper.selectById(1L, 601L)).thenReturn(doseTask(601L, LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1)));

        CheckinCorrectRequest request = new CheckinCorrectRequest();
        request.setResult("TAKEN");

        assertThatThrownBy(() -> appMedicationService.correctCheckin("13800138000", 701L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("DOSE_TASK_EDIT_WINDOW_EXPIRED");
    }

    @Test
    void shouldSupersedeEffectiveCheckinWhenCorrectionSucceeds() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(medicationCheckinRecordMapper.selectById(1L, 701L)).thenReturn(checkinRecord(701L, 601L, true));

        MedicationDoseTask task = doseTask(601L, LocalDateTime.now(ZoneOffset.UTC).plusHours(2));
        when(medicationDoseTaskMapper.selectById(1L, 601L)).thenReturn(task);
        doAnswer(invocation -> {
            MedicationCheckinRecord record = invocation.getArgument(0);
            record.setId(702L);
            return 1;
        }).when(medicationCheckinRecordMapper).insert(any(MedicationCheckinRecord.class));

        CheckinCorrectRequest request = new CheckinCorrectRequest();
        request.setResult("MISSED");
        request.setSkipReason("PATIENT_REPORTED");
        request.setNote("late correction");

        CheckinRecordDTO result = appMedicationService.correctCheckin("13800138000", 701L, request);

        verify(medicationCheckinRecordMapper).markIneffectiveByDoseTaskId(eq(1L), eq(601L), eq("app-checkin-correct"), any(LocalDateTime.class));
        verify(medicationDoseTaskMapper).updateStatus(task);
        assertThat(task.getLastEffectiveCheckinId()).isEqualTo(702L);
        assertThat(task.getStatus()).isEqualTo("MISSED");
        assertThat(result.getId()).isEqualTo(702L);
        assertThat(result.getCorrection()).isTrue();
        assertThat(result.getSupersedesRecordId()).isEqualTo(701L);
        assertThat(result.getEffective()).isTrue();
    }

    private AppPatientService.AccessContext accessContext() {
        AppAccount account = new AppAccount();
        account.setId(101L);
        account.setMobile("13800138000");
        account.setAccountType("PATIENT");

        Patient patient = new Patient();
        patient.setId(201L);
        patient.setAppAccountId(101L);

        return new AppPatientService.AccessContext(account, patient, "PATIENT");
    }

    private MedicationCheckinRecord checkinRecord(Long id, Long doseTaskId, boolean effective) {
        MedicationCheckinRecord record = new MedicationCheckinRecord();
        record.setId(id);
        record.setDoseTaskId(doseTaskId);
        record.setPatientId(201L);
        record.setIsEffective(effective);
        return record;
    }

    private MedicationDoseTask doseTask(Long id, LocalDateTime lockedAfter) {
        MedicationDoseTask task = new MedicationDoseTask();
        task.setId(id);
        task.setPatientId(201L);
        task.setLockedAfter(lockedAfter);
        task.setStatus("COMPLETED");
        return task;
    }
}
