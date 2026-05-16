package com.medicine.service.ocr;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Profile("test")
public class MockOcrService implements OcrService {

    @Override
    public OcrResult performOcr(byte[] imageBytes, String contentType) {
        OcrItem item = new OcrItem();
        item.setDrugName("Aspirin Enteric-coated Tablet");
        item.setDrugSpec("100mg");
        item.setDosage("100mg");
        item.setFrequency("once daily");
        item.setRoute("oral");
        item.setDurationDays(30);
        item.setNote("test OCR draft");
        item.setConfidence(new BigDecimal("0.92"));
        return OcrResult.success(
                "Aspirin Enteric-coated Tablet 100mg once daily oral",
                List.of(item),
                new BigDecimal("0.92"),
                "{\"rawText\":\"Aspirin Enteric-coated Tablet 100mg once daily oral\",\"overallConfidence\":0.92,\"items\":[{\"drugName\":\"Aspirin Enteric-coated Tablet\"}]}"
        );
    }
}
