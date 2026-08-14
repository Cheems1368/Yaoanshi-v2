package com.medicine.service.speech;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.config.SpeechProperties;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XfyunSpeechTranscriptionServiceTest {

    @Test
    void shouldRetryFallbackForUnavailableDomainAndTransportErrors() {
        XfyunSpeechTranscriptionService service = createService("xfime-mianqie", "iat");

        assertTrue(service.shouldRetryFallback("11200"));
        assertTrue(service.shouldRetryFallback("SPEECH_CALL_FAILED"));
        assertTrue(service.shouldRetryFallback("SPEECH_WEBSOCKET_ERROR"));
    }

    @Test
    void shouldNotRetryFallbackForOtherErrorsOrEquivalentDomain() {
        XfyunSpeechTranscriptionService service = createService("xfime-mianqie", "iat");

        assertFalse(service.shouldRetryFallback("SPEECH_TIMEOUT"));
        assertFalse(service.shouldRetryFallback("SPEECH_EMPTY_RESULT"));
        assertFalse(createService("iat", "iat").shouldRetryFallback("SPEECH_CALL_FAILED"));
        assertFalse(createService("iat", "").shouldRetryFallback("SPEECH_CALL_FAILED"));
    }

    @Test
    void shouldFormatSingleDigitDayAsTwoDigitsForXfyunAuth() {
        ZonedDateTime timestamp = ZonedDateTime.of(2026, 8, 1, 14, 19, 23, 0, ZoneOffset.UTC);

        assertEquals("Sat, 01 Aug 2026 14:19:23 GMT", XfyunSpeechTranscriptionService.formatAuthDate(timestamp));
    }

    private XfyunSpeechTranscriptionService createService(String primaryDomain, String fallbackDomain) {
        SpeechProperties properties = new SpeechProperties();
        properties.setPrimaryDomain(primaryDomain);
        properties.setFallbackDomain(fallbackDomain);
        return new XfyunSpeechTranscriptionService(properties, new ObjectMapper());
    }
}
