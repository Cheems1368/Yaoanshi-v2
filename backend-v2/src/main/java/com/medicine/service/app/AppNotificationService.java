package com.medicine.service.app;

import com.medicine.api.dto.notification.NotificationLogDTO;
import com.medicine.domain.notification.NotificationLog;
import com.medicine.repository.notification.NotificationLogMapper;
import com.medicine.service.auth.AppPatientService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class AppNotificationService {
    private final NotificationLogMapper notificationLogMapper;
    private final AppPatientService appPatientService;

    public AppNotificationService(NotificationLogMapper notificationLogMapper, AppPatientService appPatientService) {
        this.notificationLogMapper = notificationLogMapper;
        this.appPatientService = appPatientService;
    }

    public List<NotificationLogDTO> list(String subject, Long afterId, int limit) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        int normalizedLimit = Math.min(Math.max(limit, 1), 100);
        return notificationLogMapper.selectPatientLogs(1L, context.patient().getId(), afterId, normalizedLimit)
                .stream().map(this::toDto).toList();
    }

    private NotificationLogDTO toDto(NotificationLog log) {
        NotificationLogDTO dto = new NotificationLogDTO();
        dto.setId(log.getId()); dto.setTargetType(log.getTargetType()); dto.setTargetId(log.getTargetId());
        dto.setChannel(log.getChannel()); dto.setBusinessType(log.getBusinessType()); dto.setBusinessId(log.getBusinessId());
        dto.setScheduledAt(format(log.getScheduledAt())); dto.setSentAt(format(log.getSentAt()));
        dto.setDeliveryStatus(log.getDeliveryStatus()); dto.setFailureCode(log.getFailureCode());
        dto.setFailureReason(log.getFailureReason()); dto.setPayloadJson(log.getPayloadJson());
        return dto;
    }

    private String format(LocalDateTime value) { return value == null ? null : value.toInstant(ZoneOffset.UTC).toString(); }
}
