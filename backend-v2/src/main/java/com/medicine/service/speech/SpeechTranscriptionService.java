package com.medicine.service.speech;

public interface SpeechTranscriptionService {

    SpeechResult transcribePcm16k(byte[] audioBytes);

    class SpeechResult {
        private final boolean success;
        private final String text;
        private final String provider;
        private final String errorCode;

        private SpeechResult(boolean success, String text, String provider, String errorCode) {
            this.success = success;
            this.text = text;
            this.provider = provider;
            this.errorCode = errorCode;
        }

        public static SpeechResult success(String text, String provider) {
            return new SpeechResult(true, text, provider, null);
        }

        public static SpeechResult failure(String errorCode, String provider) {
            return new SpeechResult(false, null, provider, errorCode);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getText() {
            return text;
        }

        public String getProvider() {
            return provider;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
