package com.medicine.service.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.domain.notification.NotificationLog;
import com.medicine.repository.notification.NotificationLogMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

    @Mock
    private NotificationLogMapper notificationLogMapper;

    @Mock
    private AdminNotificationRealtimeService realtimeService;

    @Mock
    private AppNotificationRealtimeService appRealtimeService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NotificationDispatchService notificationDispatchService;

    @Test
    void shouldMarkProfessionalNotificationAsSentWhenRealtimeDispatchSucceeds() {
        doAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            log.setId(9001L);
            return 1;
        }).when(notificationLogMapper).insert(any(NotificationLog.class));
        when(realtimeService.dispatch(any(NotificationLog.class))).thenReturn(NotificationDispatchService.DispatchResult.sent());

        notificationDispatchService.createAndDispatch(
                1L,
                "unit-test",
                "PHARMACIST",
                101L,
                "HELP_ORDER_SUBMITTED",
                6001L,
                Map.of("status", "SUBMITTED"),
                LocalDateTime.now()
        );

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogMapper).updateDelivery(captor.capture());
        NotificationLog updated = captor.getValue();
        assertThat(updated.getChannel()).isEqualTo("WEBSOCKET");
        assertThat(updated.getDeliveryStatus()).isEqualTo("SENT");
        assertThat(updated.getSentAt()).isNotNull();
        assertThat(updated.getFailureCode()).isNull();
    }

    @Test
    void shouldDegradeProfessionalNotificationToInAppWhenNoRealtimeSessionExists() {
        doAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            log.setId(9002L);
            return 1;
        }).when(notificationLogMapper).insert(any(NotificationLog.class));
        when(realtimeService.dispatch(any(NotificationLog.class)))
                .thenReturn(NotificationDispatchService.DispatchResult.failed("NO_ACTIVE_SESSION", "No active WebSocket session"));

        notificationDispatchService.createAndDispatch(
                1L,
                "unit-test",
                "DOCTOR",
                202L,
                "HELP_ORDER_ESCALATED",
                6002L,
                Map.of("status", "WAITING_DOCTOR"),
                LocalDateTime.now()
        );

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogMapper).updateDelivery(captor.capture());
        NotificationLog updated = captor.getValue();
        assertThat(updated.getChannel()).isEqualTo("IN_APP");
        assertThat(updated.getDeliveryStatus()).isEqualTo("SENT");
        assertThat(updated.getFailureCode()).isNull();
        assertThat(updated.getSentAt()).isNotNull();
    }

    @Test
    void shouldTreatAppNotificationsAsSentWithoutRealtimeDispatch() {
        doAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            log.setId(9003L);
            return 1;
        }).when(notificationLogMapper).insert(any(NotificationLog.class));
        when(appRealtimeService.dispatch(any(NotificationLog.class))).thenReturn(NotificationDispatchService.DispatchResult.sent());

        notificationDispatchService.createAndDispatch(
                1L,
                "unit-test",
                "PATIENT",
                301L,
                "HELP_ORDER_CLOSED",
                6003L,
                Map.of("status", "CLOSED"),
                LocalDateTime.now()
        );

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogMapper).updateDelivery(captor.capture());
        NotificationLog updated = captor.getValue();
        assertThat(updated.getChannel()).isEqualTo("WEBSOCKET");
        assertThat(updated.getDeliveryStatus()).isEqualTo("SENT");
        assertThat(updated.getSentAt()).isNotNull();
    }

    @Test
    void shouldScheduleBoundedRetryForRealSendFailure() {
        doAnswer(invocation -> { ((NotificationLog) invocation.getArgument(0)).setId(9004L); return 1; })
                .when(notificationLogMapper).insert(any(NotificationLog.class));
        when(realtimeService.dispatch(any(NotificationLog.class)))
                .thenReturn(NotificationDispatchService.DispatchResult.failed("SOCKET_SEND_FAILED", "send failed"));
        LocalDateTime now = LocalDateTime.now();

        notificationDispatchService.createAndDispatch(1L, "unit-test", "PHARMACIST", 101L,
                "HELP_ORDER_SUBMITTED", 6004L, Map.of(), now);

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogMapper).updateDelivery(captor.capture());
        NotificationLog updated = captor.getValue();
        assertThat(updated.getDeliveryStatus()).isEqualTo("FAILED");
        assertThat(updated.getAttemptCount()).isEqualTo(1);
        assertThat(updated.getNextRetryAt()).isEqualTo(now.plusMinutes(1));
    }

    @Test
    void shouldStopRetryingAfterFourthFailedAttempt() {
        NotificationLog log = new NotificationLog();
        log.setId(9005L);
        log.setChannel("WEBSOCKET");
        log.setTargetType("PHARMACIST");
        log.setDeliveryStatus("FAILED");
        log.setAttemptCount(3);
        log.setClaimToken("claim-9005");
        when(realtimeService.dispatch(log))
                .thenReturn(NotificationDispatchService.DispatchResult.failed("SOCKET_SEND_FAILED", "send failed"));
        LocalDateTime now = LocalDateTime.now();

        notificationDispatchService.retryClaimed(log, now);

        assertThat(log.getDeliveryStatus()).isEqualTo("PERMANENT_FAILED");
        assertThat(log.getAttemptCount()).isEqualTo(4);
        assertThat(log.getNextRetryAt()).isNull();
        verify(notificationLogMapper).updateClaimedDelivery(log, "claim-9005");
    }
}
