package com.medicine.service.ai;

import com.medicine.api.dto.ai.AiConsentGrantRequest;
import com.medicine.api.dto.ai.AiDisclosureDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.AiProperties;
import com.medicine.domain.ai.AiConsentRecord;
import com.medicine.domain.identity.AppAccount;
import com.medicine.repository.ai.AiConsentRecordMapper;
import com.medicine.repository.identity.AppAccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiConsentServiceTest {

    @Mock
    private AppAccountMapper appAccountMapper;
    @Mock
    private AiConsentRecordMapper consentRecordMapper;

    private AiConsentService service;

    @BeforeEach
    void setUp() {
        AiProperties properties = new AiProperties();
        AiConsentPolicy policy = new AiConsentPolicy(properties);
        service = new AiConsentService(appAccountMapper, consentRecordMapper, policy);
    }

    @Test
    void legacyConsentTimestampAloneShouldNotGrantV2Consent() {
        when(appAccountMapper.selectActiveByMobile(1L, "13800000000")).thenReturn(account());
        when(consentRecordMapper.selectCurrent(anyLong(), anyLong(), anyString(), anyString(), anyString())).thenReturn(null);

        AiDisclosureDTO disclosure = service.getDisclosure("13800000000");

        assertThat(disclosure.isConsentGranted()).isFalse();
        assertThat(disclosure.getPolicyVersion()).isEqualTo("sec-005-v2");
        assertThat(disclosure.getProviderDisplayName()).isEqualTo("硅基流动");
        assertThat(disclosure.getStatement()).contains("不保存您的问题或 AI 回复正文", "保留 180 天");
    }

    @Test
    void shouldRejectMissingOrStalePolicyVersion() {
        AiConsentGrantRequest request = new AiConsentGrantRequest();
        request.setPolicyVersion("sec-005-v1");

        BusinessException exception = catchThrowableOfType(
                () -> service.grantConsent("13800000000", request),
                BusinessException.class
        );

        assertThat(exception.getCode()).isEqualTo(ErrorCode.AI_CONSENT_POLICY_MISMATCH.getCode());
        verify(consentRecordMapper, never()).insert(any());
    }

    @Test
    void shouldPersistVersionedConsentAndLegacyTimestamp() {
        AiConsentGrantRequest request = new AiConsentGrantRequest();
        request.setPolicyVersion("sec-005-v2");
        when(appAccountMapper.selectActiveByMobile(1L, "13800000000")).thenReturn(account());
        when(consentRecordMapper.insert(any(AiConsentRecord.class))).thenReturn(1);

        service.grantConsent("13800000000", request);

        ArgumentCaptor<AiConsentRecord> captor = ArgumentCaptor.forClass(AiConsentRecord.class);
        verify(consentRecordMapper).insert(captor.capture());
        assertThat(captor.getValue().getPolicyVersion()).isEqualTo("sec-005-v2");
        assertThat(captor.getValue().getProviderCode()).isEqualTo("SILICONFLOW");
        assertThat(captor.getValue().getStatementHash()).hasSize(64);
        verify(appAccountMapper).updateAiConsent(anyLong(), any(java.time.LocalDateTime.class));
    }

    private AppAccount account() {
        AppAccount account = new AppAccount();
        account.setId(1001L);
        account.setTenantId(1L);
        account.setAiConsentGrantedAt(java.time.LocalDateTime.now().minusDays(1));
        return account;
    }
}
