package com.medicine.service.ocr;

import java.math.BigDecimal;

public interface OcrService {

    OcrResult performOcr(byte[] imageBytes, String contentType);

    class OcrResult {
        private final boolean success;
        private final String extractedText;
        private final BigDecimal confidence;
        private final String responseJson;

        private OcrResult(boolean success, String extractedText, BigDecimal confidence, String responseJson) {
            this.success = success;
            this.extractedText = extractedText;
            this.confidence = confidence;
            this.responseJson = responseJson;
        }

        public static OcrResult success(String extractedText, BigDecimal confidence, String responseJson) {
            return new OcrResult(true, extractedText, confidence, responseJson);
        }

        public static OcrResult failure(String responseJson) {
            return new OcrResult(false, null, BigDecimal.ZERO, responseJson);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getExtractedText() {
            return extractedText;
        }

        public BigDecimal getConfidence() {
            return confidence;
        }

        public String getResponseJson() {
            return responseJson;
        }
    }
}
