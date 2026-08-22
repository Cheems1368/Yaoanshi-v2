package com.medicine.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.AiProperties;
import com.medicine.service.ai.AiUpstreamClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class HttpAiUpstreamClient implements AiUpstreamClient {

    private static final Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1");

    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI endpoint;

    public HttpAiUpstreamClient(AiProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build());
    }

    HttpAiUpstreamClient(AiProperties properties, ObjectMapper objectMapper, HttpClient httpClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        this.endpoint = validateEndpoint(properties);
    }

    @Override
    public String chat(List<Map<String, String>> messages) {
        validateRuntimeConfig();
        Map<String, Object> payload = buildPayload(messages);

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(endpoint)
                    .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode == HttpStatus.PAYMENT_REQUIRED.value() || statusCode == HttpStatus.FORBIDDEN.value()) {
                String body = response.body();
                if (body != null && body.contains("\"code\":30001")) {
                    throw new BusinessException(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode(), "AI 账户余额不足，请先充值后再试");
                }
            }
            if (statusCode == HttpStatus.TOO_MANY_REQUESTS.value()) {
                throw new BusinessException(ErrorCode.AI_CHAT_RATE_LIMITED.getCode(), "AI 服务繁忙，请稍后重试");
            }
            if (statusCode == HttpStatus.UNAUTHORIZED.value() || statusCode == HttpStatus.FORBIDDEN.value()) {
                throw new BusinessException(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode(), "AI 服务认证失败，请联系管理员");
            }
            if (statusCode >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                throw new BusinessException(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode(), "AI 服务暂不可用，请稍后重试");
            }
            if (statusCode < 200 || statusCode >= 300) {
                throw new BusinessException(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode(), "AI 服务请求失败");
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode contentNode = extractContentNode(root);
            if (contentNode.isMissingNode() || !StringUtils.hasText(contentNode.asText())) {
                throw new BusinessException(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode(), "AI 响应格式异常");
            }
            return contentNode.asText().trim();
        } catch (BusinessException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode(), "AI 服务调用被中断");
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode(), "AI 服务网络异常，请稍后重试");
        }
    }

    @Override
    public String providerHost() {
        return endpoint.getHost().toLowerCase(Locale.ROOT);
    }

    private void validateRuntimeConfig() {
        if (!StringUtils.hasText(properties.getApiKey()) || !StringUtils.hasText(properties.getModel())) {
            throw new BusinessException(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode(), "AI 服务尚未配置，请联系管理员");
        }
    }

    private URI validateEndpoint(AiProperties properties) {
        try {
            URI uri = URI.create(properties.getBaseUrl());
            if (!StringUtils.hasText(uri.getHost()) || uri.getUserInfo() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException("AI base URL must be an absolute host URL without user info or fragment");
            }

            String host = uri.getHost().toLowerCase(Locale.ROOT);
            Set<String> allowedHosts = properties.getAllowedHosts() == null ? Set.of() : properties.getAllowedHosts().stream()
                    .filter(StringUtils::hasText)
                    .map(value -> value.trim().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toUnmodifiableSet());
            if (!allowedHosts.contains(host)) {
                throw new IllegalArgumentException("AI base URL host is not allowlisted: " + host);
            }

            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            boolean secure = "https".equals(scheme);
            boolean explicitLoopbackHttp = "http".equals(scheme) && LOOPBACK_HOSTS.contains(host);
            if (!secure && !explicitLoopbackHttp) {
                throw new IllegalArgumentException("AI base URL must use HTTPS; HTTP is allowed only for allowlisted loopback hosts");
            }
            return uri;
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Invalid medicine.ai.base-url configuration", ex);
        }
    }

    private Map<String, Object> buildPayload(List<Map<String, String>> messages) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", properties.getModel());
        payload.put("messages", messages);

        if (properties.getProtocol() == AiProperties.Protocol.ANTHROPIC_MESSAGES) {
            if (properties.getMaxTokens() != null && properties.getMaxTokens() > 0) {
                payload.put("max_tokens", properties.getMaxTokens());
            }
            return payload;
        }

        payload.put("stream", false);
        payload.put("temperature", 0.3);
        payload.put("enable_thinking", properties.isEnableThinking());
        if (properties.getMaxTokens() != null && properties.getMaxTokens() > 0) {
            payload.put("max_tokens", properties.getMaxTokens());
        }
        return payload;
    }

    private JsonNode extractContentNode(JsonNode root) {
        if (properties.getProtocol() == AiProperties.Protocol.ANTHROPIC_MESSAGES) {
            JsonNode contentNode = root.path("content");
            if (contentNode.isArray()) {
                for (JsonNode item : contentNode) {
                    if ("text".equals(item.path("type").asText()) && StringUtils.hasText(item.path("text").asText())) {
                        return item.path("text");
                    }
                }
            }
            return root.path("content").path(0).path("text");
        }
        return root.path("choices").path(0).path("message").path("content");
    }
}
