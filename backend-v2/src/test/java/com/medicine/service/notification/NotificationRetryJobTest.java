package com.medicine.service.notification;

import com.medicine.domain.notification.NotificationLog;
import com.medicine.repository.notification.NotificationLogMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationRetryJobTest {
    @Mock private NotificationLogMapper notificationLogMapper;
    @Mock private NotificationDispatchService dispatchService;

    @Test
    void shouldDispatchOnlyAfterCasClaimSucceeds() {
        NotificationLog candidate = new NotificationLog();
        candidate.setId(11L); candidate.setTenantId(1L); candidate.setDeliveryStatus("FAILED");
        when(notificationLogMapper.selectRetryCandidates(any(), any(), eq(50))).thenReturn(List.of(candidate));
        when(notificationLogMapper.claimRetry(eq(1L), eq(11L), anyString(), any(), any())).thenReturn(1);
        when(notificationLogMapper.selectClaimed(eq(11L), anyString())).thenReturn(candidate);

        new NotificationRetryJob(notificationLogMapper, dispatchService).retryDueNotifications();

        verify(dispatchService).retryClaimed(eq(candidate), any());
    }

    @Test
    void shouldSkipWhenAnotherWorkerWonClaim() {
        NotificationLog candidate = new NotificationLog();
        candidate.setId(11L); candidate.setTenantId(1L);
        when(notificationLogMapper.selectRetryCandidates(any(), any(), eq(50))).thenReturn(List.of(candidate));
        when(notificationLogMapper.claimRetry(eq(1L), eq(11L), anyString(), any(), any())).thenReturn(0);

        new NotificationRetryJob(notificationLogMapper, dispatchService).retryDueNotifications();

        verify(dispatchService, never()).retryClaimed(any(), any());
    }
}
