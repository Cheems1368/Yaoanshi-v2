package com.medicine.service.admin;

import com.medicine.api.dto.notification.NotificationLogDTO;
import com.medicine.common.model.PageResult;
import com.medicine.domain.notification.NotificationLog;
import com.medicine.repository.notification.NotificationLogMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class AdminNotificationService {

    private final NotificationLogMapper notificationLogMapper;
    private final ProfessionalIdentityService professionalIdentityService;

    public AdminNotificationService(NotificationLogMapper notificationLogMapper, ProfessionalIdentityService professionalIdentityService) {
        this.notificationLogMapper = notificationLogMapper;
        this.professionalIdentityService = professionalIdentityService;
    }

    public PageResult<NotificationLogDTO> list(JwtClaims claims, String businessType, String deliveryStatus, int pageNo, int pageSize) {
        professionalIdentityService.getOrCreatePharmacistProfile(claims);
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.max(pageSize, 1);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;
        long total = notificationLogMapper.count(claims.getTenantId(), blankToNull(businessType), blankToNull(deliveryStatus));
        List<NotificationLogDTO> rows = notificationLogMapper.search(
                        claims.getTenantId(),
                        blankToNull(businessType),
                        blankToNull(deliveryStatus),
                        offset,
                        normalizedPageSize
                ).stream()
                .map(this::toDto)
                .toList();
        return new PageResult<>(normalizedPageNo, normalizedPageSize, total, rows);
    }

    private NotificationLogDTO toDto(NotificationLog log) {
        NotificationLogDTO dto = new NotificationLogDTO();
        dto.setId(log.getId());
        dto.setTargetType(log.getTargetType());
        dto.setTargetId(log.getTargetId());
        dto.setChannel(log.getChannel());
        dto.setBusinessType(log.getBusinessType());
        dto.setBusinessId(log.getBusinessId());
        dto.setScheduledAt(formatDateTime(log.getScheduledAt()));
        dto.setSentAt(formatDateTime(log.getSentAt()));
        dto.setDeliveryStatus(log.getDeliveryStatus());
        dto.setFailureCode(log.getFailureCode());
        dto.setFailureReason(log.getFailureReason());
        dto.setPayloadJson(log.getPayloadJson());
        return dto;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }
}
