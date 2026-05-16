package com.medicine.service.ocr;

import java.math.BigDecimal;
import java.util.List;

public interface OcrService {

    OcrResult performOcr(byte[] imageBytes, String contentType);

    class OcrResult {
        private final boolean success;
        private final String extractedText;
        private final List<OcrItem> items;
        private final BigDecimal confidence;
        private final String responseJson;
        private final String failureReason;

        private OcrResult(boolean success, String extractedText, List<OcrItem> items, BigDecimal confidence, String responseJson, String failureReason) {
            this.success = success;
            this.extractedText = extractedText;
            this.items = items == null ? List.of() : items;
            this.confidence = confidence;
            this.responseJson = responseJson;
            this.failureReason = failureReason;
        }

        public static OcrResult success(String extractedText, BigDecimal confidence, String responseJson) {
            return new OcrResult(true, extractedText, List.of(), confidence, responseJson, null);
        }

        public static OcrResult success(String extractedText, List<OcrItem> items, BigDecimal confidence, String responseJson) {
            return new OcrResult(true, extractedText, items, confidence, responseJson, null);
        }

        public static OcrResult failure(String responseJson) {
            return new OcrResult(false, null, List.of(), BigDecimal.ZERO, responseJson, responseJson);
        }

        public static OcrResult failure(String responseJson, String failureReason) {
            return new OcrResult(false, null, List.of(), BigDecimal.ZERO, responseJson, failureReason);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getExtractedText() {
            return extractedText;
        }

        public List<OcrItem> getItems() {
            return items;
        }

        public BigDecimal getConfidence() {
            return confidence;
        }

        public String getResponseJson() {
            return responseJson;
        }

        public String getFailureReason() {
            return failureReason;
        }
    }

    class OcrItem {
        private String drugName;
        private String drugSpec;
        private String dosage;
        private String frequency;
        private String route;
        private Integer durationDays;
        private String note;
        private BigDecimal confidence;

        public String getDrugName() {
            return drugName;
        }

        public void setDrugName(String drugName) {
            this.drugName = drugName;
        }

        public String getDrugSpec() {
            return drugSpec;
        }

        public void setDrugSpec(String drugSpec) {
            this.drugSpec = drugSpec;
        }

        public String getDosage() {
            return dosage;
        }

        public void setDosage(String dosage) {
            this.dosage = dosage;
        }

        public String getFrequency() {
            return frequency;
        }

        public void setFrequency(String frequency) {
            this.frequency = frequency;
        }

        public String getRoute() {
            return route;
        }

        public void setRoute(String route) {
            this.route = route;
        }

        public Integer getDurationDays() {
            return durationDays;
        }

        public void setDurationDays(Integer durationDays) {
            this.durationDays = durationDays;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public BigDecimal getConfidence() {
            return confidence;
        }

        public void setConfidence(BigDecimal confidence) {
            this.confidence = confidence;
        }
    }
}
