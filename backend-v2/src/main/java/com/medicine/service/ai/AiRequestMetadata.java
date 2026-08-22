package com.medicine.service.ai;

public record AiRequestMetadata(
        int originalMessageCount,
        int transmittedMessageCount,
        int originalCharCount,
        int transmittedCharCount,
        int redactionCount
) {
}
