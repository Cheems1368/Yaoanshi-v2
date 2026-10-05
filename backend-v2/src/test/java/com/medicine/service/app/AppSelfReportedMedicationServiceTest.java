package com.medicine.service.app;

import com.medicine.api.dto.medication.SelfReportedMedicationCreateRequest;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.medication.SelfReportedMedication;
import com.medicine.repository.helporder.HelpOrderEventMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.medication.SelfReportedMedicationMapper;
import com.medicine.service.auth.AppPatientService;
import com.medicine.service.notification.NotificationDispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppSelfReportedMedicationServiceTest {
    @Mock private SelfReportedMedicationMapper selfReportedMedicationMapper;
    @Mock private HelpOrderMapper helpOrderMapper;
    @Mock private HelpOrderEventMapper helpOrderEventMapper;
    @Mock private NotificationDispatchService notificationDispatchService;
    @Mock private AppPatientService appPatientService;
    @InjectMocks private AppSelfReportedMedicationService service;

    @BeforeEach
    void preparePatient() {
        AppAccount account = new AppAccount();
        account.setId(101L);
        Patient patient = new Patient();
        patient.setId(201L);
        patient.setBoundByPharmacistId(301L);
        when(appPatientService.requireAccessContext("patient")).thenReturn(
                new AppPatientService.AccessContext(account, patient, "PATIENT"));
    }

    @Test
    void optionalNotesMustStillProduceRequiredOrderDescription() {
        SelfReportedMedicationCreateRequest request = new SelfReportedMedicationCreateRequest();
        SelfReportedMedicationCreateRequest.DrugEntry drug = new SelfReportedMedicationCreateRequest.DrugEntry();
        drug.setDrugName("合成药物");
        request.setDrugs(List.of(drug));

        service.create("patient", request, "NORMAL");

        ArgumentCaptor<HelpOrder> order = ArgumentCaptor.forClass(HelpOrder.class);
        verify(helpOrderMapper).insert(order.capture());
        assertThat(order.getValue().getDescription()).isNotBlank().contains("合成药物");
    }

    @Test
    void withdrawalKeepsStoppedMedicationInPatientHistory() {
        SelfReportedMedication medication = new SelfReportedMedication();
        medication.setId(401L);
        medication.setPatientId(201L);
        medication.setStatus("PENDING");
        medication.setDeleted(false);
        when(selfReportedMedicationMapper.selectById(1L, 401L)).thenReturn(medication);

        service.withdraw("patient", 401L);

        verify(selfReportedMedicationMapper).update(medication);
        assertThat(medication.getStatus()).isEqualTo("STOPPED");
        assertThat(medication.getDeleted()).isFalse();
    }
}
