package com.medicine.service.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.config.OcrProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekOcrService implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekOcrService.class);

    private static final String OCR_PROMPT =
            "请提取这张处方图片中的所有文字内容，包括药品名称、规格、剂量、用法用量、患者姓名、诊断等信息。" +
            "按原文输出，保持原始格式。如果图片不清晰或非处方，请说明原因。";

    private static final BigDecimal HIGH_CONFIDENCE = new BigDecimal("0.85");
    private static final BigDecimal LOW_CONFIDENCE = new BigDecimal("0.30");
    private static final int MIN_MEANINGFUL_CHARS = 10;

    private final OcrProperties ocrProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public DeepSeekOcrService(OcrProperties ocrProperties, ObjectMapper objectMapper) {
        this.ocrProperties = ocrProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(ocrProperties.getTimeoutMs()))
                .build();
    }

    @Override
    public OcrResult performOcr(String imageUrl) {
        if (!StringUtils.hasText(ocrProperties.getApiKey())) {
            log.info("OCR API key not configured, skipping OCR for image: {}", imageUrl);
            return OcrResult.failure(null);
        }

        try {
            Map<String, Object> payload = buildPayload(imageUrl);
            String requestJson = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ocrProperties.getBaseUrl()))
                    .timeout(Duration.ofMillis(ocrProperties.getTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + ocrProperties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("OCR upstream returned HTTP {}: {}", response.statusCode(), responseBody);
                return OcrResult.failure(responseBody);
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || !StringUtils.hasText(contentNode.asText())) {
                log.warn("OCR response missing content field");
                return OcrResult.failure(responseBody);
            }

            String extractedText = contentNode.asText().trim();
            BigDecimal confidence = extractedText.length() >= MIN_MEANINGFUL_CHARS
                    ? HIGH_CONFIDENCE
                    : LOW_CONFIDENCE;

            log.info("OCR completed, extracted {} chars, confidence={}", extractedText.length(), confidence);
            return OcrResult.success(extractedText, confidence, responseBody);

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("OCR call interrupted for image: {}", imageUrl);
            return OcrResult.failure(null);
        } catch (Exception ex) {
            log.warn("OCR call failed for image {}: {}", imageUrl, ex.getMessage());
            return OcrResult.failure(null);
        }
    }

    private Map<String, Object> buildPayload(String imageUrl) {
        Map<String, Object> imageContent = new LinkedHashMap<>();
        imageContent.put("type", "image_url");
        imageContent.put("image_url", Map.of("url", imageUrl));

        Map<String, Object> textContent = new LinkedHashMap<>();
        textContent.put("type", "text");
        textContent.put("text", OCR_PROMPT);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", List.of(imageContent, textContent));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", ocrProperties.getModel());
        payload.put("messages", List.of(message));
        payload.put("stream", false);
        payload.put("max_tokens", 2048);
        return payload;
    }
}
