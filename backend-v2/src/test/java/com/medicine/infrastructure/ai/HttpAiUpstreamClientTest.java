package com.medicine.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.AiProperties;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HttpAiUpstreamClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldRejectHostOutsideExactAllowlist() {
        AiProperties properties = configuredProperties();
        properties.setBaseUrl("https://evil.example/v1/chat/completions");

        assertThatThrownBy(() -> new HttpAiUpstreamClient(properties, objectMapper))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid medicine.ai.base-url");
    }

    @Test
    void shouldRejectNonLoopbackHttpEvenWhenHostIsAllowlisted() {
        AiProperties properties = configuredProperties();
        properties.setBaseUrl("http://api.siliconflow.cn/v1/chat/completions");

        assertThatThrownBy(() -> new HttpAiUpstreamClient(properties, objectMapper))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldAllowExplicitLoopbackHttpForTests() {
        AiProperties properties = configuredProperties();
        properties.setBaseUrl("http://127.0.0.1:18081/v1/chat/completions");
        properties.setAllowedHosts(List.of("127.0.0.1"));

        HttpAiUpstreamClient client = new HttpAiUpstreamClient(properties, objectMapper);

        assertThat(client.providerHost()).isEqualTo("127.0.0.1");
    }

    @Test
    void shouldStopBeforeNetworkWhenApiKeyIsMissing() throws Exception {
        AiProperties properties = configuredProperties();
        properties.setApiKey("");
        HttpClient httpClient = mock(HttpClient.class);
        HttpAiUpstreamClient client = new HttpAiUpstreamClient(properties, objectMapper, httpClient);

        BusinessException exception = catchThrowableOfType(
                () -> client.chat(List.of(Map.of("role", "user", "content", "safe"))),
                BusinessException.class
        );

        assertThat(exception.getCode()).isEqualTo(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode());
        verify(httpClient, never()).send(
                any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()
        );
    }

    @Test
    void shouldNormalizeAuthenticationFailure() throws Exception {
        AiProperties properties = configuredProperties();
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(httpClient.send(
                any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()
        )).thenReturn(response);
        when(response.statusCode()).thenReturn(401);
        HttpAiUpstreamClient client = new HttpAiUpstreamClient(properties, objectMapper, httpClient);

        BusinessException exception = catchThrowableOfType(
                () -> client.chat(List.of(Map.of("role", "user", "content", "safe"))),
                BusinessException.class
        );

        assertThat(exception.getCode()).isEqualTo(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode());
    }

    @Test
    void shouldNormalizeNetworkFailure() throws Exception {
        AiProperties properties = configuredProperties();
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.send(
                any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()
        )).thenThrow(new IOException("network unavailable"));
        HttpAiUpstreamClient client = new HttpAiUpstreamClient(properties, objectMapper, httpClient);

        BusinessException exception = catchThrowableOfType(
                () -> client.chat(List.of(Map.of("role", "user", "content", "safe"))),
                BusinessException.class
        );

        assertThat(exception.getCode()).isEqualTo(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode());
    }

    @Test
    void shouldSendOnlyProvidedMessagesAndParseOpenAiResponse() throws Exception {
        AiProperties properties = configuredProperties();
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(httpClient.send(
                any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()
        )).thenReturn(response);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"choices\":[{\"message\":{\"content\":\"safe reply\"}}]}");
        HttpAiUpstreamClient client = new HttpAiUpstreamClient(properties, objectMapper, httpClient);

        String reply = client.chat(List.of(
                Map.of("role", "system", "content", "system"),
                Map.of("role", "user", "content", "[手机号已隐藏]")
        ));

        assertThat(reply).isEqualTo("safe reply");
        org.mockito.ArgumentCaptor<HttpRequest> captor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(
                captor.capture(),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()
        );
        JsonNode payload = objectMapper.readTree(extractBody(captor.getValue()));
        assertThat(payload.path("messages")).hasSize(2);
        assertThat(payload.path("messages").get(1).path("content").asText()).isEqualTo("[手机号已隐藏]");
    }

    private AiProperties configuredProperties() {
        AiProperties properties = new AiProperties();
        properties.setApiKey("test-key");
        properties.setAllowedHosts(List.of("api.siliconflow.cn"));
        return properties;
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
}
