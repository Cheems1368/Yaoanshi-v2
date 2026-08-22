package com.medicine.service.ai;

import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.AiProperties;
import com.medicine.domain.ai.AiRequestAuditLog;
import com.medicine.domain.identity.AppAccount;
import com.medicine.repository.ai.AiRequestAuditLogMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAuditServiceTest {

    @Mock
    private AiRequestAuditLogMapper mapper;

    @Test
    void shouldFailClosedWhenPendingAuditCannotBeInserted() {
        AiAuditService service = new AiAuditService(mapper, new AiProperties());
        when(mapper.insert(any())).thenThrow(new IllegalStateException("database unavailable"));

        BusinessException exception = catchThrowableOfType(
                () -> service.start(account(), "request-1", "api.siliconflow.cn", metadata()),
                BusinessException.class
        );

        assertThat(exception.getCode()).isEqualTo(ErrorCode.AI_AUDIT_UNAVAILABLE.getCode());
    }

    @Test
    void shouldStoreOnlyMetadataWithOneHundredEightyDayExpiry() {
        AiAuditService service = new AiAuditService(mapper, new AiProperties());
        when(mapper.insert(any())).thenAnswer(invocation -> {
            AiRequestAuditLog log = invocation.getArgument(0);
            log.setId(99L);
            return 1;
        });

        service.start(account(), "request-2", "api.siliconflow.cn", metadata());

        ArgumentCaptor<AiRequestAuditLog> captor = ArgumentCaptor.forClass(AiRequestAuditLog.class);
        verify(mapper).insert(captor.capture());
        AiRequestAuditLog audit = captor.getValue();
        assertThat(audit.getStatus()).isEqualTo("PENDING");
        assertThat(audit.getRedactionCount()).isEqualTo(2);
        assertThat(audit.getExpiresAt()).isEqualTo(audit.getCreateTime().plusDays(180));
        assertThat(AiRequestAuditLog.class.getDeclaredFields())
                .extracting(java.lang.reflect.Field::getName)
                .doesNotContain("prompt", "reply", "payload", "content", "contentHash");
    }

    @Test
    void shouldNormalizeFailedOutcomeWithoutContent() {
        AiAuditService service = new AiAuditService(mapper, new AiProperties());
        when(mapper.insert(any())).thenAnswer(invocation -> {
            AiRequestAuditLog log = invocation.getArgument(0);
            log.setId(100L);
            return 1;
        });
        when(mapper.updateOutcome(any())).thenReturn(1);
        AiRequestAuditLog audit = service.start(account(), "request-3", "api.siliconflow.cn", metadata());

        service.completeFailure(
                audit,
                new BusinessException(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode(), "provider response body must not be stored"),
                25L
        );

        ArgumentCaptor<AiRequestAuditLog> captor = ArgumentCaptor.forClass(AiRequestAuditLog.class);
        verify(mapper).updateOutcome(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("FAILED");
        assertThat(captor.getValue().getErrorCode())
                .isEqualTo(String.valueOf(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode()));
    }

    private AppAccount account() {
        AppAccount account = new AppAccount();
        account.setId(1001L);
        account.setTenantId(1L);
        return account;
    }

    private AiRequestMetadata metadata() {
        return new AiRequestMetadata(4, 3, 120, 80, 2);
    }
}
