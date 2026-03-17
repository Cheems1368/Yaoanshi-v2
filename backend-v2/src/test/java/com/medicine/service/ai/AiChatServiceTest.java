package com.medicine.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.ai.AiChatMessage;
import com.medicine.api.dto.ai.AiChatRequest;
import com.medicine.api.dto.ai.AiChatResponse;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.AiProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @Test
    void shouldRejectRequestWhenAiRuntimeConfigMissing() {
        AiProperties properties = new AiProperties();
        properties.setApiKey("");
        AiChatService service = new AiChatService(properties, new ObjectMapper(), stringRedisTemplate);

        BusinessException exception = catchThrowableOfType(
                () -> service.chat("acct-1", request(newMessage("user", "hello"))),
                BusinessException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode());
    }

    @Test
    void shouldReturnReplyWithSuggestionsWhenUpstreamCallSucceeds() throws Exception {
        AiProperties properties = new AiProperties();
        properties.setBaseUrl("https://api.siliconflow.cn/v1/messages");
        properties.setApiKey("test-key");
        properties.setModel("Qwen/Qwen3.5-122B-A10B");
        properties.setTimeoutMs(5000);
        properties.setProtocol(AiProperties.Protocol.ANTHROPIC_MESSAGES);
        properties.setMaxTokens(1024);
        properties.setUpstreamMaxMessages(4);

        String upstreamReply = "safe reply\\n---SUGGESTIONS---\\n漏服后该怎么办？\\n需要复诊吗？\\n有哪些不良反应？";

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("app:ai:chat:limit:acct-1")).thenReturn(1L);
        when(stringRedisTemplate.expire("app:ai:chat:limit:acct-1", 1, TimeUnit.MINUTES)).thenReturn(true);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"content\":[{\"type\":\"text\",\"text\":\"" + upstreamReply + "\"}]}");

        AiChatService service = new AiChatService(properties, new ObjectMapper(), stringRedisTemplate);
        ReflectionTestUtils.setField(service, "httpClient", httpClient);

        AiChatResponse response = service.chat("acct-1", request(newMessage("system", "ignored"), newMessage("user", "hello")));

        assertThat(response.getModel()).isEqualTo("Qwen/Qwen3.5-122B-A10B");
        assertThat(response.getRequestId()).isNotBlank();
        assertThat(response.getReply()).isEqualTo("safe reply");
        assertThat(response.getReply()).doesNotContain("仅供用药科普");
        assertThat(response.getSuggestions()).hasSize(3);
        assertThat(response.getSuggestions().get(0)).isEqualTo("漏服后该怎么办？");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
        JsonNode payload = new ObjectMapper().readTree(extractBody(requestCaptor.getValue()));
        assertThat(payload.path("model").asText()).isEqualTo("Qwen/Qwen3.5-122B-A10B");
        assertThat(payload.path("max_tokens").asInt()).isEqualTo(1024);
        assertThat(payload.path("messages")).hasSize(2);
    }

    @Test
    void shouldLimitReplyBodyToTwoHundredChars() throws Exception {
        AiProperties properties = new AiProperties();
        properties.setBaseUrl("https://api.siliconflow.cn/v1/chat/completions");
        properties.setApiKey("test-key");
        properties.setModel("zai-org/GLM-4.5-Air");
        properties.setTimeoutMs(5000);
        properties.setProtocol(AiProperties.Protocol.OPENAI_CHAT);
        properties.setMaxTokens(1024);
        properties.setUpstreamMaxMessages(10);

        String longReply = "这是一个较长的用药说明，主要介绍漏服后的处理原则、服药时间、常见不良反应观察、是否需要复诊以及何时尽快就医。"
                .repeat(8);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("app:ai:chat:limit:acct-1b")).thenReturn(1L);
        when(stringRedisTemplate.expire("app:ai:chat:limit:acct-1b", 1, TimeUnit.MINUTES)).thenReturn(true);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("""
                {"choices":[{"message":{"content":"%s"}}]}
                """.formatted(longReply));

        AiChatService service = new AiChatService(properties, new ObjectMapper(), stringRedisTemplate);
        ReflectionTestUtils.setField(service, "httpClient", httpClient);

        AiChatResponse response = service.chat("acct-1b", request(newMessage("user", "降压药漏服后怎么办？")));

        assertThat(response.getReply().length()).isLessThanOrEqualTo(200);
        assertThat(response.getReply()).doesNotContain("仅供用药科普");
        assertThat(response.getReply()).endsWith("...");
        assertThat(response.getSuggestions()).isNotNull();
        assertThat(response.getSuggestions()).isNotEmpty();
    }

    @Test
    void shouldTrimConversationForSiliconFlowUpstreamWindow() throws Exception {
        AiProperties properties = new AiProperties();
        properties.setBaseUrl("https://api.siliconflow.cn/v1/messages");
        properties.setApiKey("test-key");
        properties.setModel("Qwen/Qwen3.5-122B-A10B");
        properties.setTimeoutMs(5000);
        properties.setProtocol(AiProperties.Protocol.ANTHROPIC_MESSAGES);
        properties.setMaxTokens(1024);
        properties.setUpstreamMaxMessages(4);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("app:ai:chat:limit:acct-2")).thenReturn(1L);
        when(stringRedisTemplate.expire("app:ai:chat:limit:acct-2", 1, TimeUnit.MINUTES)).thenReturn(true);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"content\":[{\"type\":\"text\",\"text\":\"trimmed reply\"}]}");

        AiChatService service = new AiChatService(properties, new ObjectMapper(), stringRedisTemplate);
        ReflectionTestUtils.setField(service, "httpClient", httpClient);

        service.chat("acct-2", request(
                newMessage("system", "ignored"),
                newMessage("user", "u1"),
                newMessage("assistant", "a1"),
                newMessage("user", "u2"),
                newMessage("assistant", "a2"),
                newMessage("user", "u3")
        ));

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
        String body = extractBody(requestCaptor.getValue());
        JsonNode payload = new ObjectMapper().readTree(body);

        assertThat(payload.path("model").asText()).isEqualTo("Qwen/Qwen3.5-122B-A10B");
        assertThat(payload.path("max_tokens").asInt()).isEqualTo(1024);
        assertThat(payload.path("messages")).hasSize(4);
        assertThat(payload.path("messages").get(0).path("role").asText()).isEqualTo("system");
        assertThat(payload.path("messages").get(1).path("content").asText()).isEqualTo("u2");
        assertThat(payload.path("messages").get(2).path("content").asText()).isEqualTo("a2");
        assertThat(payload.path("messages").get(3).path("content").asText()).isEqualTo("u3");
    }

    @Test
    void shouldMapInsufficientBalanceForAnthropicMessagesProtocol() throws Exception {
        AiProperties properties = new AiProperties();
        properties.setBaseUrl("https://api.siliconflow.cn/v1/messages");
        properties.setApiKey("test-key");
        properties.setModel("Qwen/Qwen3.5-122B-A10B");
        properties.setTimeoutMs(5000);
        properties.setProtocol(AiProperties.Protocol.ANTHROPIC_MESSAGES);
        properties.setMaxTokens(1024);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("app:ai:chat:limit:acct-3")).thenReturn(1L);
        when(stringRedisTemplate.expire("app:ai:chat:limit:acct-3", 1, TimeUnit.MINUTES)).thenReturn(true);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(403);
        when(httpResponse.body()).thenReturn("{\"code\":30001,\"message\":\"Sorry, your account balance is insufficient\",\"data\":null}");

        AiChatService service = new AiChatService(properties, new ObjectMapper(), stringRedisTemplate);
        ReflectionTestUtils.setField(service, "httpClient", httpClient);

        BusinessException exception = catchThrowableOfType(
                () -> service.chat("acct-3", request(newMessage("user", "hello"))),
                BusinessException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode());
        assertThat(exception.getMessage()).isEqualTo("AI 账户余额不足，请先充值后再试");
    }

    private String extractBody(HttpRequest request) throws Exception {
        CompletableFuture<String> bodyFuture = new CompletableFuture<>();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        request.bodyPublisher().orElseThrow().subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                byte[] bytes = new byte[item.remaining()];
                item.get(bytes);
                output.write(bytes, 0, bytes.length);
            }

            @Override
            public void onError(Throwable throwable) {
                bodyFuture.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                bodyFuture.complete(output.toString(StandardCharsets.UTF_8));
            }
        });

        return bodyFuture.get();
    }

    private AiChatRequest request(AiChatMessage... messages) {
        AiChatRequest request = new AiChatRequest();
        request.setMessages(List.of(messages));
        return request;
    }

    private AiChatMessage newMessage(String role, String content) {
        AiChatMessage message = new AiChatMessage();
        message.setRole(role);
        message.setContent(content);
        return message;
    }
}
