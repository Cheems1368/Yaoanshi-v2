package com.medicine.service.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.config.OcrProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RapidOcrServiceTest {
    private RapidOcrService service(OcrProperties properties) {
        ObjectMapper mapper = new ObjectMapper();
        return new RapidOcrService(properties, mapper, new PrescriptionOcrParser(mapper));
    }

    @Test
    void shouldRejectCloudAddressesAndInvalidImagesBeforeSending() {
        OcrProperties properties = new OcrProperties();
        properties.setBaseUrl("https://api.siliconflow.cn/v1/chat/completions");
        assertThat(service(properties).performOcr(new byte[]{1}, "image/png").getFailureReason()).isEqualTo("OCR_LOCAL_URL_REJECTED");
        assertThat(service(properties).performOcr(new byte[0], "image/png").getFailureReason()).isEqualTo("OCR_IMAGE_INVALID");
        assertThat(service(properties).performOcr(new byte[]{1}, "text/plain").getFailureReason()).isEqualTo("OCR_IMAGE_INVALID");
        for (String address : new String[]{"http://localhost:19090/ocr", "http://127.0.0.1:19090/ocr?url=x",
                "http://user@127.0.0.1:19090/ocr", "http://127.0.0.1:19091/ocr"}) {
            properties.setBaseUrl(address);
            assertThat(service(properties).performOcr(new byte[]{1}, "image/png").getFailureReason()).isEqualTo("OCR_LOCAL_URL_REJECTED");
        }
    }

    @Test
    void shouldHandleResponsesWithoutPersistingPayloadOrFollowingRedirects() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 19090), 0);
        AtomicInteger redirected = new AtomicInteger();
        server.createContext("/destination", exchange -> { redirected.incrementAndGet(); exchange.close(); });
        server.createContext("/ocr", exchange -> {
            byte[] input = exchange.getRequestBody().readAllBytes();
            if (input[0] == 4) {
                try { Thread.sleep(150); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            }
            String body = input[0] == 1 ? "{\"blocks\":[]}" : "{\"patient\":\"sensitive\",\"blocks\":\"bad\"}";
            if (input[0] == 3) {
                exchange.getResponseHeaders().add("Location", "http://127.0.0.1:19090/destination");
                exchange.sendResponseHeaders(302, -1);
            } else {
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            }
            exchange.close();
        });
        server.start();
        try {
            RapidOcrService service = service(new OcrProperties());
            assertThat(service.performOcr(new byte[]{1}, "image/png").getFailureReason()).isEqualTo("OCR_NO_TEXT");
            var malformed = service.performOcr(new byte[]{2}, "image/png");
            assertThat(malformed.getFailureReason()).isEqualTo("OCR_RESPONSE_INVALID");
            assertThat(malformed.getResponseJson()).doesNotContain("sensitive");
            assertThat(service.performOcr(new byte[]{3}, "image/png").getFailureReason()).isEqualTo("OCR_LOCAL_HTTP_302");
            assertThat(redirected.get()).isZero();
            OcrProperties timeout = new OcrProperties();
            timeout.setTimeoutMs(20);
            assertThat(service(timeout).performOcr(new byte[]{4}, "image/png").getFailureReason()).isEqualTo("OCR_LOCAL_TIMEOUT");
        } finally {
            server.stop(0);
        }
        assertThat(service(new OcrProperties()).performOcr(new byte[]{1}, "image/png").getFailureReason())
                .isEqualTo("OCR_LOCAL_UNAVAILABLE_OR_INVALID_RESPONSE");
    }
}
