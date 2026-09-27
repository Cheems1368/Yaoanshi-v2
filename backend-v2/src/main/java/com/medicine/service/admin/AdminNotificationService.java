package com.medicine.service.admin;

import com.medicine.api.dto.notification.NotificationLogDTO;
import com.medicine.common.model.PageResult;
import com.medicine.domain.notification.NotificationLog;
import com.medicine.repository.notification.NotificationLogMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@Service
public class AdminNotificationService {

    private final NotificationLogMapper notificationLogMapper;
    private final ProfessionalIdentityService professionalIdentityService;

    public AdminNotificationService(NotificationLogMapper notificationLogMapper, ProfessionalIdentityService professionalIdentityService) {
        this.notificationLogMapper = notificationLogMapper;
        this.professionalIdentityService = professionalIdentityService;
    }

    public PageResult<NotificationLogDTO> list(JwtClaims claims, String businessType, String deliveryStatus, int pageNo, int pageSize) {
        if (claims == null || !claims.isProfessional() || claims.getTenantId() == null || claims.getRoles() == null ||
                claims.getRoles().stream().noneMatch(role -> role != null && Set.of("admin", "super_admin", "tenant_admin").contains(role.toLowerCase()))) {
            throw new BusinessException(ErrorCode.PATIENT_MANAGEMENT_PERMISSION_DENIED.getCode(),
                    ErrorCode.PATIENT_MANAGEMENT_PERMISSION_DENIED.getMessage());
        }
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

    public Cursor baseline(JwtClaims claims) {
        Target target = requireTarget(claims);
        Long latest = notificationLogMapper.selectLatestPersonalLogId(claims.getTenantId(), target.type(), target.id());
        return new Cursor(latest == null ? 0L : latest);
    }

    public PersonalPage personalLogs(JwtClaims claims, long afterId, int limit) {
        Target target = requireTarget(claims);
        int size = Math.min(Math.max(limit, 1), 100);
        List<NotificationLog> rows = notificationLogMapper.selectPersonalLogs(claims.getTenantId(), target.type(), target.id(),
                Math.max(afterId, 0), size + 1);
        boolean hasMore = rows.size() > size;
        List<NotificationLogDTO> page = rows.stream().limit(size).map(this::toDto).toList();
        long nextCursor = page.isEmpty() ? Math.max(afterId, 0) : page.get(page.size() - 1).getId();
        return new PersonalPage(page, nextCursor, hasMore);
    }

    private Target requireTarget(JwtClaims claims) {
        if (claims == null || !claims.isProfessional() || claims.getTenantId() == null) {
            throw new BusinessException(ErrorCode.ADMIN_AUTH_INVALID.getCode(), ErrorCode.ADMIN_AUTH_INVALID.getMessage());
        }
        if (professionalIdentityService.isPharmacist(claims)) {
            return new Target("PHARMACIST", professionalIdentityService.getOrCreatePharmacistProfile(claims).getId());
        }
        if (professionalIdentityService.isDoctor(claims)) {
            return new Target("DOCTOR", professionalIdentityService.getOrCreateDoctorProfile(claims).getId());
        }
        throw new BusinessException(ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getCode(), ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getMessage());
    }

    private record Target(String type, Long id) {}
    public record Cursor(Long cursor) {}
    public record PersonalPage(List<NotificationLogDTO> list, long nextCursor, boolean hasMore) {}

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
