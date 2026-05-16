package com.medicine.service.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.config.OcrProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("!test")
public class DeepSeekOcrService implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekOcrService.class);

    private static final String OCR_PROMPT = """
            请只提取处方图片中的用药明细，不要提取患者姓名、身份证号、手机号等身份信息。
            必须只返回 JSON，不要返回 Markdown。
            JSON 格式：
            {
              "rawText": "与用药明细相关的原文",
              "overallConfidence": 0.0,
              "items": [
                {
                  "drugName": "药品名称",
                  "drugSpec": "规格，可为空",
                  "dosage": "剂量，可为空",
                  "frequency": "频次，可为空",
                  "route": "给药途径，可为空",
                  "durationDays": 7,
                  "note": "备注，可为空",
                  "confidence": 0.0
                }
              ]
            }
            如果不是处方或无法识别，请返回 {"rawText":"","overallConfidence":0,"items":[]}。
            """;

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
    public OcrResult performOcr(byte[] imageBytes, String contentType) {
        if (!StringUtils.hasText(ocrProperties.getApiKey())) {
            log.info("OCR API key not configured, skipping OCR.");
            return OcrResult.failure("OCR_API_KEY_MISSING");
        }
        if (imageBytes == null || imageBytes.length == 0) {
            log.warn("OCR image bytes are empty.");
            return OcrResult.failure("OCR_IMAGE_EMPTY");
        }

        try {
            Map<String, Object> payload = buildPayload(imageBytes, contentType);
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
                log.warn("OCR upstream returned HTTP {}", response.statusCode());
                return OcrResult.failure("OCR_UPSTREAM_HTTP_" + response.statusCode(), "OCR_UPSTREAM_HTTP_" + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || !StringUtils.hasText(contentNode.asText())) {
                log.warn("OCR response missing content field");
                return OcrResult.failure("OCR_RESPONSE_MISSING_CONTENT");
            }

            StructuredOcr structured = parseStructuredContent(contentNode.asText());
            log.info("OCR completed, extracted {} chars, items={}, confidence={}",
                    structured.rawText().length(), structured.items().size(), structured.confidence());
            return OcrResult.success(structured.rawText(), structured.items(), structured.confidence(), structured.safeResponseJson());

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("OCR call interrupted.");
            return OcrResult.failure("OCR_CALL_INTERRUPTED");
        } catch (Exception ex) {
            log.warn("OCR call failed: {}", ex.getMessage());
            return OcrResult.failure("OCR_CALL_FAILED");
        }
    }

    private Map<String, Object> buildPayload(byte[] imageBytes, String contentType) {
        String normalizedContentType = StringUtils.hasText(contentType) ? contentType.trim().toLowerCase() : "image/jpeg";
        String dataUrl = "data:" + normalizedContentType + ";base64," + Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> imageContent = new LinkedHashMap<>();
        imageContent.put("type", "image_url");
        imageContent.put("image_url", Map.of("url", dataUrl));

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

    private StructuredOcr parseStructuredContent(String content) throws Exception {
        String json = stripJsonFence(content == null ? "" : content.trim());
        try {
            JsonNode root = objectMapper.readTree(json);
            String rawText = root.path("rawText").asText("");
            BigDecimal confidence = root.hasNonNull("overallConfidence")
                    ? BigDecimal.valueOf(root.path("overallConfidence").asDouble(0))
                    : LOW_CONFIDENCE;
            List<OcrItem> items = new ArrayList<>();
            JsonNode itemNodes = root.path("items");
            if (itemNodes.isArray()) {
                for (JsonNode itemNode : itemNodes) {
                    String drugName = itemNode.path("drugName").asText("").trim();
                    if (!StringUtils.hasText(drugName)) {
                        continue;
                    }
                    OcrItem item = new OcrItem();
                    item.setDrugName(drugName);
                    item.setDrugSpec(textOrNull(itemNode, "drugSpec"));
                    item.setDosage(textOrNull(itemNode, "dosage"));
                    item.setFrequency(textOrNull(itemNode, "frequency"));
                    item.setRoute(textOrNull(itemNode, "route"));
                    item.setNote(textOrNull(itemNode, "note"));
                    if (itemNode.hasNonNull("durationDays") && itemNode.path("durationDays").canConvertToInt()) {
                        int days = itemNode.path("durationDays").asInt();
                        item.setDurationDays(days > 0 ? days : null);
                    }
                    if (itemNode.hasNonNull("confidence")) {
                        item.setConfidence(BigDecimal.valueOf(itemNode.path("confidence").asDouble(0)));
                    }
                    items.add(item);
                }
            }
            if (items.isEmpty() && rawText.length() >= MIN_MEANINGFUL_CHARS) {
                confidence = confidence.min(LOW_CONFIDENCE);
            }
            return new StructuredOcr(rawText, confidence, items, objectMapper.writeValueAsString(root));
        } catch (Exception ex) {
            log.warn("OCR structured JSON parse failed: {}", ex.getMessage());
            String rawText = content == null ? "" : content.trim();
            BigDecimal confidence = rawText.length() >= MIN_MEANINGFUL_CHARS ? LOW_CONFIDENCE : BigDecimal.ZERO;
            return new StructuredOcr(rawText, confidence, List.of(), objectMapper.writeValueAsString(Map.of(
                    "rawText", rawText,
                    "overallConfidence", confidence,
                    "items", List.of()
            )));
        }
    }

    private String stripJsonFence(String content) {
        if (content.startsWith("```")) {
            return content.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
        }
        return content;
    }

    private String textOrNull(JsonNode node, String field) {
        String value = node.path(field).asText("").trim();
        return StringUtils.hasText(value) ? value : null;
    }

    private record StructuredOcr(String rawText, BigDecimal confidence, List<OcrItem> items, String safeResponseJson) {
    }
}
