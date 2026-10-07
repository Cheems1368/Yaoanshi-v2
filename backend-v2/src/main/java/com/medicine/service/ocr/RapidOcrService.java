package com.medicine.service.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.config.OcrProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Profile("!test")
public class RapidOcrService implements OcrService {

    private static final Set<String> CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/webp");
    private final OcrProperties properties;
    private final ObjectMapper objectMapper;
    private final PrescriptionOcrParser parser;
    private final HttpClient client;

    public RapidOcrService(OcrProperties properties, ObjectMapper objectMapper, PrescriptionOcrParser parser) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.parser = parser;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .followRedirects(HttpClient.Redirect.NEVER)
                .proxy(new ProxySelector() {
                    @Override
                    public List<Proxy> select(URI uri) { return List.of(Proxy.NO_PROXY); }
                    @Override
                    public void connectFailed(URI uri, SocketAddress address, IOException error) { }
                }).build();
    }

    @Override
    public OcrResult performOcr(byte[] imageBytes, String contentType) {
        if (imageBytes == null || imageBytes.length == 0 || imageBytes.length > 10 * 1024 * 1024
                || contentType == null || !CONTENT_TYPES.contains(contentType)) {
            return OcrResult.failure("OCR_IMAGE_INVALID");
        }
        try {
            URI uri = URI.create(properties.getBaseUrl());
            if (!"http".equals(uri.getScheme()) || !"127.0.0.1".equals(uri.getHost())
                    || uri.getPort() != 19090 || !"/ocr".equals(uri.getPath())
                    || uri.getRawQuery() != null || uri.getRawFragment() != null || uri.getRawUserInfo() != null) {
                return OcrResult.failure("OCR_LOCAL_URL_REJECTED");
            }
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                    .header("Content-Type", contentType)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(imageBytes)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return OcrResult.failure("OCR_LOCAL_HTTP_" + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode nodes = root.path("blocks");
            if (!nodes.isArray()) {
                return OcrResult.failure("OCR_RESPONSE_INVALID");
            }
            List<PrescriptionOcrParser.TextBlock> blocks = new ArrayList<>();
            for (JsonNode node : nodes) {
                if (!node.path("text").isTextual() || !node.path("score").isNumber()
                        || !node.path("box").isArray() || node.path("box").size() != 4) {
                    return OcrResult.failure("OCR_RESPONSE_INVALID");
                }
                double[][] box = new double[4][2];
                for (int i = 0; i < 4; i++) {
                    JsonNode point = node.path("box").get(i);
                    if (!point.isArray() || point.size() != 2 || !point.get(0).isNumber() || !point.get(1).isNumber()) {
                        return OcrResult.failure("OCR_RESPONSE_INVALID");
                    }
                    box[i][0] = point.get(0).asDouble(Double.NaN);
                    box[i][1] = point.get(1).asDouble(Double.NaN);
                }
                PrescriptionOcrParser.TextBlock block = new PrescriptionOcrParser.TextBlock(
                        node.get("text").asText(), node.get("score").asDouble(Double.NaN), box);
                if (!block.valid()) {
                    return OcrResult.failure("OCR_RESPONSE_INVALID");
                }
                blocks.add(block);
            }
            return parser.parse(blocks);
        } catch (HttpTimeoutException ex) {
            return OcrResult.failure("OCR_LOCAL_TIMEOUT");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return OcrResult.failure("OCR_CALL_INTERRUPTED");
        } catch (IOException ex) {
            return OcrResult.failure("OCR_LOCAL_UNAVAILABLE_OR_INVALID_RESPONSE");
        } catch (RuntimeException ex) {
            return OcrResult.failure("OCR_RESPONSE_INVALID");
        }
    }
}
