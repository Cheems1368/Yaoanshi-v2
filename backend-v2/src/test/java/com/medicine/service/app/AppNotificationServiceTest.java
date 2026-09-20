package com.medicine.service.app;

import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.repository.notification.NotificationLogMapper;
import com.medicine.service.auth.AppPatientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppNotificationServiceTest {

    @Mock
    private NotificationLogMapper notificationLogMapper;

    @Mock
    private AppPatientService appPatientService;

    @InjectMocks
    private AppNotificationService appNotificationService;

    @Test
    void shouldReturnLatestPatientNotificationIdForColdStartCursor() {
        Patient patient = new Patient();
        patient.setId(88L);
        when(appPatientService.requireAccessContext("13800138000"))
                .thenReturn(new AppPatientService.AccessContext(new AppAccount(), patient, "FAMILY"));
        when(notificationLogMapper.selectLatestPatientLogId(1L, 88L)).thenReturn(321L);

        assertThat(appNotificationService.latestId("13800138000")).isEqualTo(321L);
    }

    @Test
    void shouldReturnZeroWhenPatientHasNoNotificationLogs() {
        Patient patient = new Patient();
        patient.setId(88L);
        when(appPatientService.requireAccessContext("13800138000"))
                .thenReturn(new AppPatientService.AccessContext(new AppAccount(), patient, "PATIENT"));
        when(notificationLogMapper.selectLatestPatientLogId(1L, 88L)).thenReturn(null);

        assertThat(appNotificationService.latestId("13800138000")).isZero();
    }
}
