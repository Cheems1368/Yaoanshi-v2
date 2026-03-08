package com.medicine.service.admin;

import com.medicine.api.dto.notification.NotificationLogDTO;
import com.medicine.common.model.PageResult;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.domain.notification.NotificationLog;
import com.medicine.repository.notification.NotificationLogMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminNotificationServiceTest {

    @Mock
    private NotificationLogMapper notificationLogMapper;

    @Mock
    private ProfessionalIdentityService professionalIdentityService;

    @InjectMocks
    private AdminNotificationService adminNotificationService;

    @Test
    void shouldNormalizeFiltersAndMapNotificationLogPage() {
        JwtClaims claims = pharmacistClaims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacistProfile());
        when(notificationLogMapper.count(1L, "HELP_ORDER", "FAILED")).thenReturn(1L);
        when(notificationLogMapper.search(1L, "HELP_ORDER", "FAILED", 0, 1)).thenReturn(List.of(notificationLog()));

        PageResult<NotificationLogDTO> result = adminNotificationService.list(claims, "  HELP_ORDER  ", " FAILED ", 0, 0);

        assertThat(result.getPageNo()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(1);
        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getScheduledAt()).isEqualTo("2026-03-07T10:15:00Z");
        assertThat(result.getList().get(0).getPayloadJson()).isEqualTo("{\"orderNo\":\"HO1\"}");
        verify(notificationLogMapper).count(1L, "HELP_ORDER", "FAILED");
        verify(notificationLogMapper).search(1L, "HELP_ORDER", "FAILED", 0, 1);
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

    private NotificationLog notificationLog() {
        NotificationLog log = new NotificationLog();
        log.setId(55L);
        log.setTargetType("PHARMACIST");
        log.setTargetId(101L);
        log.setChannel("WEBSOCKET");
        log.setBusinessType("HELP_ORDER");
        log.setBusinessId(900L);
        log.setScheduledAt(LocalDateTime.of(2026, 3, 7, 10, 15));
        log.setSentAt(LocalDateTime.of(2026, 3, 7, 10, 16));
        log.setDeliveryStatus("FAILED");
        log.setFailureCode("SOCKET_SEND_FAILED");
        log.setFailureReason("All WebSocket sessions failed");
        log.setPayloadJson("{\"orderNo\":\"HO1\"}");
        return log;
    }
}
