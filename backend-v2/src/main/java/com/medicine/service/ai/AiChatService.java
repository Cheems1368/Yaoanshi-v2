package com.medicine.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.ai.AiChatMessage;
import com.medicine.api.dto.ai.AiChatRequest;
import com.medicine.api.dto.ai.AiChatResponse;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.AiProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AiChatService {

    private static final int MAX_MESSAGES = 20;
    private static final int MAX_MESSAGE_CHARS = 1200;
    private static final int MAX_REPLY_CHARS = 600;
    private static final int MAX_BODY_CHARS = 200;
    private static final int MAX_SUGGESTIONS = 3;
    private static final String SUGGESTIONS_DELIMITER = "---SUGGESTIONS---";
    private static final int MAX_REQUESTS_PER_MINUTE = 15;
    private static final String LIMIT_KEY_PREFIX = "app:ai:chat:limit:";
    private static final Set<String> SUPPORTED_ROLES = Set.of("user", "assistant", "system");
    private static final String DEFAULT_REPLY = "请告诉我药名、用途、服用方式和你担心的问题，我会用科普方式简要说明。";
    private static final List<String> DEFAULT_SUGGESTIONS = List.of(
            "饭前饭后吃药怎么判断？",
            "老年人多药联用注意什么？",
            "什么时候需要线下复诊？"
    );
    private static final String SYSTEM_PROMPT = """
            你是“药按时”项目中的用药科普助手，不是泛化闲聊助手。
            只回答常见用药知识、服药注意事项、漏服处理、常见不良反应观察、复诊提醒和就医建议。
            不得提供诊断、处方、个体化剂量调整、停药决定或替代医生面诊。
            如果涉及胸痛、呼吸困难、出血、意识障碍、持续高热、严重过敏等风险，必须明确提醒尽快线下就医。
            输出要直接、简短、易懂，优先回答用户的用药疑问，不要泛泛自我介绍。
            正文控制在 200 字以内。
            在正文结束后，另起一行输出 ---SUGGESTIONS--- 然后换行输出 3 个与当前对话相关的跟进问题，每行一个，不要加序号。
            """;

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final HttpClient httpClient;

    public AiChatService(AiProperties aiProperties,
                         ObjectMapper objectMapper,
                         StringRedisTemplate stringRedisTemplate) {
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(aiProperties.getTimeoutMs())).build();
    }

    public AiChatResponse chat(String accountId, AiChatRequest request) {
        validateRuntimeConfig();
        enforceRateLimit(accountId);
        List<Map<String, String>> messages = buildUpstreamMessages(request);
        String reply = callUpstream(messages);


        ParsedReply parsed = parseReply(reply);

        AiChatResponse response = new AiChatResponse();
        response.setReply(parsed.body);
        response.setSuggestions(parsed.suggestions);
        response.setModel(aiProperties.getModel());
        response.setRequestId(UUID.randomUUID().toString());
        return response;
    }

    private void validateRuntimeConfig() {
        if (!StringUtils.hasText(aiProperties.getBaseUrl())
                || !StringUtils.hasText(aiProperties.getApiKey())
                || !StringUtils.hasText(aiProperties.getModel())) {
            throw new BusinessException(ErrorCode.AI_UPSTREAM_UNAVAILABLE.getCode(), "AI 服务尚未配置，请联系管理员");
        }
    }

    private void enforceRateLimit(String accountId) {
        try {
            String key = LIMIT_KEY_PREFIX + accountId;
            Long used = stringRedisTemplate.opsForValue().increment(key);
            if (used != null && used == 1L) {
                stringRedisTemplate.expire(key, 1, TimeUnit.MINUTES);
            }
            if (used != null && used > MAX_REQUESTS_PER_MINUTE) {
                throw new BusinessException(ErrorCode.AI_CHAT_RATE_LIMITED.getCode(), "请求过于频繁，请稍后再试");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ignore) {
            // Redis 不可用时降级为不拦截，避免阻塞主业务。
        }
    }

    private List<Map<String, String>> buildUpstreamMessages(AiChatRequest request) {
        if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new BusinessException(ErrorCode.AI_CHAT_INVALID_REQUEST.getCode(), "消息不能为空");
        }

        if (request.getMessages().size() > MAX_MESSAGES) {
            throw new BusinessException(ErrorCode.AI_CHAT_INVALID_REQUEST.getCode(), "消息数量超过限制");
        }

        List<Map<String, String>> conversation = new ArrayList<>();

        int userMessageCount = 0;
        for (AiChatMessage message : request.getMessages()) {
            if (message == null || !StringUtils.hasText(message.getRole()) || !StringUtils.hasText(message.getContent())) {
                throw new BusinessException(ErrorCode.AI_CHAT_INVALID_REQUEST.getCode(), "存在空消息或非法消息");
            }

            String role = message.getRole().trim().toLowerCase();
            String content = message.getContent().trim();
            if (!SUPPORTED_ROLES.contains(role)) {
                throw new BusinessException(ErrorCode.AI_CHAT_INVALID_REQUEST.getCode(), "消息角色不支持");
            }
            if (content.length() > MAX_MESSAGE_CHARS) {
                throw new BusinessException(ErrorCode.AI_CHAT_INVALID_REQUEST.getCode(), "单条消息过长");
            }

            // 统一由服务端注入系统提示词，忽略外部 system 角色输入。
            if ("system".equals(role)) {
                continue;
            }
            if ("user".equals(role)) {
                userMessageCount++;
            }

            Map<String, String> record = new LinkedHashMap<>();
            record.put("role", role);
            record.put("content", content);
            conversation.add(record);
        }

        if (userMessageCount == 0) {
            throw new BusinessException(ErrorCode.AI_CHAT_INVALID_REQUEST.getCode(), "至少需要一条用户消息");
        }

        int upstreamMaxMessages = Math.max(aiProperties.getUpstreamMaxMessages(), 2);
        int conversationLimit = upstreamMaxMessages - 1;
        List<Map<String, String>> trimmedConversation = conversation;
        if (conversation.size() > conversationLimit) {
            trimmedConversation = new ArrayList<>(conversation.subList(conversation.size() - conversationLimit, conversation.size()));
        }

        List<Map<String, String>> normalized = new ArrayList<>(trimmedConversation.size() + 1);
        normalized.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        normalized.addAll(trimmedConversation);
        return normalized;
    }

    private String callUpstream(List<Map<String, String>> messages) {
        Map<String, Object> payload = buildPayload(messages);

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(aiProperties.getBaseUrl()))
                    .timeout(Duration.ofMillis(aiProperties.getTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiProperties.getApiKey())
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

    private Map<String, Object> buildPayload(List<Map<String, String>> messages) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", aiProperties.getModel());
        payload.put("messages", messages);

        if (aiProperties.getProtocol() == AiProperties.Protocol.ANTHROPIC_MESSAGES) {
            if (aiProperties.getMaxTokens() != null && aiProperties.getMaxTokens() > 0) {
                payload.put("max_tokens", aiProperties.getMaxTokens());
            }
            return payload;
        }

        payload.put("stream", false);
        payload.put("temperature", 0.3);
        payload.put("enable_thinking", aiProperties.isEnableThinking());
        if (aiProperties.getMaxTokens() != null && aiProperties.getMaxTokens() > 0) {
            payload.put("max_tokens", aiProperties.getMaxTokens());
        }
        return payload;
    }

    private JsonNode extractContentNode(JsonNode root) {
        if (aiProperties.getProtocol() == AiProperties.Protocol.ANTHROPIC_MESSAGES) {
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

    private static class ParsedReply {
        final String body;
        final List<String> suggestions;

        ParsedReply(String body, List<String> suggestions) {
            this.body = body;
            this.suggestions = suggestions;
        }
    }

    private ParsedReply parseReply(String answer) {
        if (!StringUtils.hasText(answer)) {
            return new ParsedReply(DEFAULT_REPLY, DEFAULT_SUGGESTIONS);
        }

        String normalized = answer
                .replace("\r\n", "\n")
                .replaceAll("[ \\t\\x0B\\f]+", " ")
                .replaceAll("\\n{3,}", "\\n\\n")
                .trim();

        if (!StringUtils.hasText(normalized)) {
            return new ParsedReply(DEFAULT_REPLY, DEFAULT_SUGGESTIONS);
        }

        String body;
        List<String> suggestions;

        int delimiterIndex = normalized.indexOf(SUGGESTIONS_DELIMITER);
        if (delimiterIndex >= 0) {
            body = normalized.substring(0, delimiterIndex).trim();
            String suggestionsBlock = normalized.substring(delimiterIndex + SUGGESTIONS_DELIMITER.length()).trim();
            suggestions = parseSuggestions(suggestionsBlock);
        } else {
            body = normalized;
            suggestions = DEFAULT_SUGGESTIONS;
        }

        if (!StringUtils.hasText(body)) {
            body = DEFAULT_REPLY;
        }

        body = truncate(body, MAX_BODY_CHARS);

        return new ParsedReply(body, suggestions);
    }

    private List<String> parseSuggestions(String block) {
        if (!StringUtils.hasText(block)) {
            return DEFAULT_SUGGESTIONS;
        }

        List<String> result = new ArrayList<>();
        for (String line : block.split("\n")) {
            String trimmed = line.replaceAll("^[\\d.、\\-\\s]+", "").trim();
            if (StringUtils.hasText(trimmed) && result.size() < MAX_SUGGESTIONS) {
                result.add(trimmed);
            }
        }

        return result.isEmpty() ? DEFAULT_SUGGESTIONS : result;
    }

    private String truncate(String text, int maxChars) {
        if (text.length() <= maxChars) {
            return text;
        }
        if (maxChars <= 3) {
            return text.substring(0, maxChars);
        }
        return text.substring(0, maxChars - 3).trim() + "...";
    }
}
