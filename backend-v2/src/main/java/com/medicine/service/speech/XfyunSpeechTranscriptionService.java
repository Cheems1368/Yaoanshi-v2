package com.medicine.service.speech;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.config.SpeechProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Profile("!test")
public class XfyunSpeechTranscriptionService implements SpeechTranscriptionService {

    private static final Logger log = LoggerFactory.getLogger(XfyunSpeechTranscriptionService.class);
    private static final String PROVIDER = "XFYUN";
    private static final int FRAME_SIZE = 1280;
    private static final DateTimeFormatter AUTH_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

    private final SpeechProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public XfyunSpeechTranscriptionService(SpeechProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .build();
    }

    @Override
    public SpeechResult transcribePcm16k(byte[] audioBytes) {
        if (!StringUtils.hasText(properties.getAppId())
                || !StringUtils.hasText(properties.getApiKey())
                || !StringUtils.hasText(properties.getApiSecret())) {
            return SpeechResult.failure("SPEECH_API_KEY_MISSING", PROVIDER);
        }
        if (audioBytes == null || audioBytes.length == 0) {
            return SpeechResult.failure("SPEECH_AUDIO_EMPTY", PROVIDER);
        }

        SpeechResult primary = transcribeWithDomain(audioBytes, properties.getPrimaryDomain());
        if (!primary.isSuccess() && shouldRetryFallback(primary.getErrorCode())) {
            log.info("XFYUN speech primary domain unavailable, retrying fallback domain.");
            return transcribeWithDomain(audioBytes, properties.getFallbackDomain());
        }
        return primary;
    }

    boolean shouldRetryFallback(String errorCode) {
        return StringUtils.hasText(properties.getFallbackDomain())
                && !properties.getFallbackDomain().equals(properties.getPrimaryDomain())
                && ("11200".equals(errorCode)
                || "SPEECH_CALL_FAILED".equals(errorCode)
                || "SPEECH_WEBSOCKET_ERROR".equals(errorCode));
    }

    private SpeechResult transcribeWithDomain(byte[] audioBytes, String domain) {
        try {
            URI uri = URI.create(buildAuthUrl());
            CountDownLatch done = new CountDownLatch(1);
            XfyunListener listener = new XfyunListener(done, objectMapper);
            WebSocket webSocket = httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                    .buildAsync(uri, listener)
                    .join();
            sendFrames(webSocket, audioBytes, domain);
            boolean completed = done.await(properties.getTimeoutMs(), TimeUnit.MILLISECONDS);
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done").join();
            if (!completed) {
                return SpeechResult.failure("SPEECH_TIMEOUT", PROVIDER);
            }
            if (listener.errorCode.get() != null) {
                return SpeechResult.failure(listener.errorCode.get(), PROVIDER);
            }
            String text = listener.text.toString().trim();
            if (!StringUtils.hasText(text)) {
                return SpeechResult.failure("SPEECH_EMPTY_RESULT", PROVIDER);
            }
            return SpeechResult.success(text, PROVIDER);
        } catch (Exception ex) {
            log.warn("XFYUN speech call failed: {}", ex.getMessage());
            return SpeechResult.failure("SPEECH_CALL_FAILED", PROVIDER);
        }
    }

    private void sendFrames(WebSocket webSocket, byte[] audioBytes, String domain) throws Exception {
        List<byte[]> frames = splitFrames(audioBytes);
        for (int i = 0; i < frames.size(); i++) {
            int status = i == 0 ? 0 : (i == frames.size() - 1 ? 2 : 1);
            Map<String, Object> payload = buildFramePayload(frames.get(i), status, domain);
            webSocket.sendText(objectMapper.writeValueAsString(payload), true).join();
            if (status != 2) {
                Thread.sleep(40);
            }
        }
    }

    private Map<String, Object> buildFramePayload(byte[] frame, int status, String domain) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", status);
        data.put("format", "audio/L16;rate=16000");
        data.put("encoding", "raw");
        data.put("audio", Base64.getEncoder().encodeToString(frame));

        Map<String, Object> payload = new LinkedHashMap<>();
        if (status == 0) {
            payload.put("common", Map.of("app_id", properties.getAppId()));
            payload.put("business", Map.of(
                    "language", "zh_cn",
                    "domain", StringUtils.hasText(domain) ? domain : "iat",
                    "accent", "mandarin",
                    "vad_eos", 5000
            ));
        }
        payload.put("data", data);
        return payload;
    }

    private List<byte[]> splitFrames(byte[] audioBytes) {
        List<byte[]> frames = new ArrayList<>();
        for (int offset = 0; offset < audioBytes.length; offset += FRAME_SIZE) {
            int length = Math.min(FRAME_SIZE, audioBytes.length - offset);
            byte[] frame = new byte[length];
            System.arraycopy(audioBytes, offset, frame, 0, length);
            frames.add(frame);
        }
        return frames.isEmpty() ? List.of(new byte[0]) : frames;
    }

    private String buildAuthUrl() throws Exception {
        URI uri = URI.create(properties.getBaseUrl());
        String host = uri.getHost();
        String path = StringUtils.hasText(uri.getRawPath()) ? uri.getRawPath() : "/";
        String date = formatAuthDate(ZonedDateTime.now(java.time.ZoneOffset.UTC));
        String signatureOrigin = "host: " + host + "\n" +
                "date: " + date + "\n" +
                "GET " + path + " HTTP/1.1";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(properties.getApiSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = Base64.getEncoder().encodeToString(mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8)));
        String authorizationOrigin = "api_key=\"" + properties.getApiKey()
                + "\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"" + signature + "\"";
        String authorization = Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));
        String query = "authorization=" + urlEncode(authorization)
                + "&date=" + urlEncode(date)
                + "&host=" + urlEncode(host);
        return properties.getBaseUrl() + "?" + query;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    static String formatAuthDate(ZonedDateTime timestamp) {
        return AUTH_DATE_FORMATTER.format(timestamp.withZoneSameInstant(java.time.ZoneOffset.UTC));
    }

    private static class XfyunListener implements WebSocket.Listener {
        private final CountDownLatch done;
        private final ObjectMapper objectMapper;
        private final StringBuilder partial = new StringBuilder();
        private final StringBuilder text = new StringBuilder();
        private final AtomicReference<String> errorCode = new AtomicReference<>();

        private XfyunListener(CountDownLatch done, ObjectMapper objectMapper) {
            this.done = done;
            this.objectMapper = objectMapper;
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            partial.append(data);
            if (last) {
                handleMessage(partial.toString());
                partial.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            errorCode.compareAndSet(null, "SPEECH_WEBSOCKET_ERROR");
            done.countDown();
        }

        private void handleMessage(String message) {
            try {
                JsonNode root = objectMapper.readTree(message);
                int code = root.path("code").asInt(0);
                if (code != 0) {
                    errorCode.compareAndSet(null, String.valueOf(code));
                    done.countDown();
                    return;
                }
                JsonNode ws = root.path("data").path("result").path("ws");
                if (ws.isArray()) {
                    for (JsonNode wordNode : ws) {
                        JsonNode cw = wordNode.path("cw");
                        if (cw.isArray() && !cw.isEmpty()) {
                            text.append(cw.get(0).path("w").asText(""));
                        }
                    }
                }
                if (root.path("data").path("status").asInt(-1) == 2) {
                    done.countDown();
                }
            } catch (Exception ex) {
                errorCode.compareAndSet(null, "SPEECH_RESPONSE_PARSE_FAILED");
                done.countDown();
            }
        }
    }
}
