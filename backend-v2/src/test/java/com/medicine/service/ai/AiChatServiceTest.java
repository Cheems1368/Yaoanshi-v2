package com.medicine.service.ai;

import com.medicine.api.dto.ai.AiChatMessage;
import com.medicine.api.dto.ai.AiChatRequest;
import com.medicine.api.dto.ai.AiChatResponse;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.AiProperties;
import com.medicine.domain.ai.AiRequestAuditLog;
import com.medicine.domain.identity.AppAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private AiConsentService consentService;
    @Mock
    private AiAuditService auditService;
    @Mock
    private AiUpstreamClient upstreamClient;

    private AiChatService service;
    private AiRequestAuditLog audit;

    @BeforeEach
    void setUp() {
        AiProperties properties = new AiProperties();
        service = new AiChatService(
                properties,
                redisTemplate,
                consentService,
                new AiPrivacyRedactor(),
                auditService,
                upstreamClient
        );
        AppAccount account = new AppAccount();
        account.setId(1001L);
        account.setTenantId(1L);
        when(consentService.requireCurrentConsent(anyString())).thenReturn(account);
        when(upstreamClient.providerHost()).thenReturn("api.siliconflow.cn");
        audit = new AiRequestAuditLog();
        audit.setId(55L);
        when(auditService.start(any(), anyString(), anyString(), any())).thenReturn(audit);
    }

    @Test
    void shouldRedactIdentifiersAndLimitUpstreamConversationToFiveMessages() {
        allowFirstRequest();
        when(upstreamClient.chat(any())).thenReturn("safe reply");

        AiChatResponse response = service.chat("13800000000", request(
                message("system", "client system must be ignored"),
                message("user", "u1"),
                message("assistant", "a1"),
                message("user", "u2"),
                message("assistant", "a2"),
                message("assistant", "a3"),
                message("user", "手机号13812345678，邮箱 patient@example.com，就诊编号：VISIT-12345，患者ID：P-12345，阿司匹林100mg怎么吃？")
        ));

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<List> messagesCaptor = ArgumentCaptor.forClass(List.class);
        verify(upstreamClient).chat(messagesCaptor.capture());
        @SuppressWarnings("unchecked")
        List<Map<String, String>> transmitted = messagesCaptor.getValue();
        assertThat(transmitted).hasSize(6);
        assertThat(transmitted.get(0).get("role")).isEqualTo("system");
        assertThat(transmitted.get(1).get("content")).isEqualTo("a1");
        assertThat(transmitted.get(5).get("content"))
                .contains("[手机号已隐藏]", "[邮箱已隐藏]", "就诊编号：[编号已隐藏]", "患者ID：[编号已隐藏]", "阿司匹林100mg")
                .doesNotContain("13812345678", "patient@example.com", "VISIT-12345", "P-12345");
        assertThat(response.getReply()).isEqualTo("safe reply");
        verify(auditService).completeSuccess(any(), anyLong());

        ArgumentCaptor<AiRequestMetadata> metadataCaptor = ArgumentCaptor.forClass(AiRequestMetadata.class);
        verify(auditService).start(any(), anyString(), anyString(), metadataCaptor.capture());
        assertThat(metadataCaptor.getValue().redactionCount()).isEqualTo(4);
        assertThat(metadataCaptor.getValue().transmittedMessageCount()).isEqualTo(6);
    }

    @Test
    void shouldOnlyAuditRedactionsInTransmittedMessages() {
        allowFirstRequest();
        when(upstreamClient.chat(any())).thenReturn("safe reply");

        service.chat("13800000000", request(
                message("user", "病历号：TRIMMED-123"),
                message("assistant", "a1"),
                message("user", "u2"),
                message("assistant", "a2"),
                message("assistant", "a3"),
                message("user", "u3")
        ));

        ArgumentCaptor<AiRequestMetadata> metadataCaptor = ArgumentCaptor.forClass(AiRequestMetadata.class);
        verify(auditService).start(any(), anyString(), anyString(), metadataCaptor.capture());
        assertThat(metadataCaptor.getValue().redactionCount()).isZero();
    }

    @Test
    void shouldNotCallUpstreamWhenAuditStartFails() {
        when(auditService.start(any(), anyString(), anyString(), any())).thenThrow(
                new BusinessException(ErrorCode.AI_AUDIT_UNAVAILABLE.getCode(), "audit unavailable")
        );

        BusinessException exception = catchThrowableOfType(
                () -> service.chat("13800000000", request(message("user", "hello"))),
                BusinessException.class
        );

        assertThat(exception.getCode()).isEqualTo(ErrorCode.AI_AUDIT_UNAVAILABLE.getCode());
        verify(upstreamClient, never()).chat(any());
    }

    @Test
    void shouldAuditRateLimitWithoutCallingUpstream() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("app:ai:chat:limit:13800000000")).thenReturn(16L);

        BusinessException exception = catchThrowableOfType(
                () -> service.chat("13800000000", request(message("user", "hello"))),
                BusinessException.class
        );

        assertThat(exception.getCode()).isEqualTo(ErrorCode.AI_CHAT_RATE_LIMITED.getCode());
        verify(auditService).completeFailure(any(), any(BusinessException.class), anyLong());
        verify(upstreamClient, never()).chat(any());
    }

    @Test
    void shouldAuditUpstreamFailure() {
        allowFirstRequest();
        BusinessException failure = new BusinessException(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode(), "unavailable");
        when(upstreamClient.chat(any())).thenThrow(failure);

        BusinessException exception = catchThrowableOfType(
                () -> service.chat("13800000000", request(message("user", "hello"))),
                BusinessException.class
        );

        assertThat(exception).isSameAs(failure);
        verify(auditService).completeFailure(any(), any(BusinessException.class), anyLong());
    }

    @Test
    void shouldParseSuggestionsAndLimitReplyBody() {
        allowFirstRequest();
        String body = "这是一个较长的用药说明。".repeat(30);
        when(upstreamClient.chat(any())).thenReturn(body + "\n---SUGGESTIONS---\n问题一？\n问题二？\n问题三？");

        AiChatResponse response = service.chat("13800000000", request(message("user", "hello")));

        assertThat(response.getReply()).hasSizeLessThanOrEqualTo(200).endsWith("...");
        assertThat(response.getSuggestions()).containsExactly("问题一？", "问题二？", "问题三？");
        assertThat(response.getRequestId()).isNotBlank();
    }

    private void allowFirstRequest() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("app:ai:chat:limit:13800000000")).thenReturn(1L);
        when(redisTemplate.expire("app:ai:chat:limit:13800000000", 1, TimeUnit.MINUTES)).thenReturn(true);
    }

    private AiChatRequest request(AiChatMessage... messages) {
        AiChatRequest request = new AiChatRequest();
        request.setMessages(List.of(messages));
        return request;
    }

    private AiChatMessage message(String role, String content) {
        AiChatMessage message = new AiChatMessage();
        message.setRole(role);
        message.setContent(content);
        return message;
    }
}
