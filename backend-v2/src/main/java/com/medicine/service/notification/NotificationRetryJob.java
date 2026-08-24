package com.medicine.service.notification;

import com.medicine.domain.notification.NotificationLog;
import com.medicine.repository.notification.NotificationLogMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "medicine.notifications.retry", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationRetryJob {
    private final NotificationLogMapper notificationLogMapper;
    private final NotificationDispatchService dispatchService;

    public NotificationRetryJob(NotificationLogMapper notificationLogMapper, NotificationDispatchService dispatchService) {
        this.notificationLogMapper = notificationLogMapper;
        this.dispatchService = dispatchService;
    }

    @Scheduled(fixedDelayString = "${medicine.notifications.retry.fixed-delay-ms:30000}")
    public void retryDueNotifications() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime staleBefore = now.minusMinutes(5);
        for (NotificationLog candidate : notificationLogMapper.selectRetryCandidates(now, staleBefore, 50)) {
            String claimToken = UUID.randomUUID().toString();
            if (notificationLogMapper.claimRetry(candidate.getTenantId(), candidate.getId(), claimToken, now, staleBefore) != 1) continue;
            dispatchService.retryClaimed(notificationLogMapper.selectClaimed(candidate.getId(), claimToken), now);
        }
    }
}
