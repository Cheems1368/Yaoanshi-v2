package com.medicine.service.ai;

import com.medicine.api.dto.ai.AiChatMessage;
import com.medicine.api.dto.ai.AiChatRequest;
import com.medicine.api.dto.ai.AiChatResponse;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.AiProperties;
import com.medicine.domain.ai.AiRequestAuditLog;
import com.medicine.domain.identity.AppAccount;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AiChatService {

    private static final int MAX_MESSAGES = 20;
    private static final int MAX_MESSAGE_CHARS = 1200;
    private static final int MAX_BODY_CHARS = 200;
    private static final int MAX_SUGGESTIONS = 3;
    private static final int MAX_TRANSMITTED_CONVERSATION_MESSAGES = 5;
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

    private final AiProperties properties;
    private final StringRedisTemplate stringRedisTemplate;
    private final AiConsentService consentService;
    private final AiPrivacyRedactor privacyRedactor;
    private final AiAuditService auditService;
    private final AiUpstreamClient upstreamClient;

    public AiChatService(AiProperties properties,
                         StringRedisTemplate stringRedisTemplate,
                         AiConsentService consentService,
                         AiPrivacyRedactor privacyRedactor,
                         AiAuditService auditService,
                         AiUpstreamClient upstreamClient) {
        this.properties = properties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.consentService = consentService;
        this.privacyRedactor = privacyRedactor;
        this.auditService = auditService;
        this.upstreamClient = upstreamClient;
    }

    public AiChatResponse chat(String mobile, AiChatRequest request) {
        AppAccount account = consentService.requireCurrentConsent(mobile);
        SanitizedConversation conversation = sanitizeConversation(request);
        String requestId = UUID.randomUUID().toString();
        long startedAt = System.nanoTime();
        AiRequestAuditLog audit = auditService.start(
                account,
                requestId,
                upstreamClient.providerHost(),
                conversation.metadata()
        );

        try {
            enforceRateLimit(mobile);
            String reply = upstreamClient.chat(conversation.messages());
            ParsedReply parsed = parseReply(reply);
            auditService.completeSuccess(audit, elapsedMillis(startedAt));

            AiChatResponse response = new AiChatResponse();
            response.setReply(parsed.body());
            response.setSuggestions(parsed.suggestions());
            response.setModel(properties.getModel());
            response.setRequestId(requestId);
            return response;
        } catch (RuntimeException ex) {
            auditService.completeFailure(audit, ex, elapsedMillis(startedAt));
            throw ex;
        }
    }

    private void enforceRateLimit(String mobile) {
        try {
            String key = LIMIT_KEY_PREFIX + mobile;
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
            // Redis unavailable must not block the optional AI feature after durable audit has started.
        }
    }

    private SanitizedConversation sanitizeConversation(AiChatRequest request) {
        if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new BusinessException(ErrorCode.AI_CHAT_INVALID_REQUEST.getCode(), "消息不能为空");
        }
        if (request.getMessages().size() > MAX_MESSAGES) {
            throw new BusinessException(ErrorCode.AI_CHAT_INVALID_REQUEST.getCode(), "消息数量超过限制");
        }

        int originalCharCount = 0;
        int userMessageCount = 0;
        List<Map<String, String>> conversation = new ArrayList<>();

        for (AiChatMessage message : request.getMessages()) {
            if (message == null || !StringUtils.hasText(message.getRole()) || !StringUtils.hasText(message.getContent())) {
                throw new BusinessException(ErrorCode.AI_CHAT_INVALID_REQUEST.getCode(), "存在空消息或非法消息");
            }

            String role = message.getRole().trim().toLowerCase();
            String content = message.getContent().trim();
            originalCharCount += content.length();
            if (!SUPPORTED_ROLES.contains(role)) {
                throw new BusinessException(ErrorCode.AI_CHAT_INVALID_REQUEST.getCode(), "消息角色不支持");
            }
            if (content.length() > MAX_MESSAGE_CHARS) {
                throw new BusinessException(ErrorCode.AI_CHAT_INVALID_REQUEST.getCode(), "单条消息过长");
            }
            if ("system".equals(role)) {
                continue;
            }
            if ("user".equals(role)) {
                userMessageCount++;
            }

            conversation.add(Map.of("role", role, "content", content));
        }

        if (userMessageCount == 0) {
            throw new BusinessException(ErrorCode.AI_CHAT_INVALID_REQUEST.getCode(), "至少需要一条用户消息");
        }

        int configuredConversationLimit = Math.max(properties.getUpstreamMaxMessages() - 1, 1);
        int conversationLimit = Math.min(configuredConversationLimit, MAX_TRANSMITTED_CONVERSATION_MESSAGES);
        List<Map<String, String>> trimmedConversation = conversation;
        if (conversation.size() > conversationLimit) {
            trimmedConversation = new ArrayList<>(conversation.subList(conversation.size() - conversationLimit, conversation.size()));
        }

        List<Map<String, String>> normalized = new ArrayList<>(trimmedConversation.size() + 1);
        normalized.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        int redactionCount = 0;
        for (Map<String, String> message : trimmedConversation) {
            AiPrivacyRedactor.RedactionResult redacted = privacyRedactor.redact(message.get("content"));
            redactionCount += redacted.redactionCount();
            normalized.add(Map.of("role", message.get("role"), "content", redacted.text()));
        }

        int transmittedCharCount = normalized.stream()
                .mapToInt(message -> message.get("content").length())
                .sum();
        AiRequestMetadata metadata = new AiRequestMetadata(
                request.getMessages().size(),
                normalized.size(),
                originalCharCount,
                transmittedCharCount,
                redactionCount
        );
        return new SanitizedConversation(normalized, metadata);
    }

    private long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
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
            suggestions = parseSuggestions(normalized.substring(delimiterIndex + SUGGESTIONS_DELIMITER.length()).trim());
        } else {
            body = normalized;
            suggestions = DEFAULT_SUGGESTIONS;
        }
        if (!StringUtils.hasText(body)) {
            body = DEFAULT_REPLY;
        }
        return new ParsedReply(truncate(body, MAX_BODY_CHARS), suggestions);
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
        return text.substring(0, maxChars - 3).trim() + "...";
    }

    private record SanitizedConversation(List<Map<String, String>> messages, AiRequestMetadata metadata) {
    }

    private record ParsedReply(String body, List<String> suggestions) {
    }
}
