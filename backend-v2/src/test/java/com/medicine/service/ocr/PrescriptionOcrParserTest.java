package com.medicine.service.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrescriptionOcrParserTest {
    private final PrescriptionOcrParser parser = new PrescriptionOcrParser(new ObjectMapper());

    private PrescriptionOcrParser.TextBlock block(String text, double score, double x, double y, double width) {
        return new PrescriptionOcrParser.TextBlock(text, score,
                new double[][]{{x, y}, {x + width, y}, {x + width, y + 40}, {x, y + 40}});
    }

    private List<PrescriptionOcrParser.TextBlock> table(double nameScore, double specScore) {
        return new ArrayList<>(List.of(block("患者：张某", .99, 80, 100, 200),
                block("药品名称", .99, 80, 200, 180), block("规格", .99, 395, 200, 100),
                block("用法", .99, 600, 200, 100), block("氨氯地平片", nameScore, 90, 300, 220),
                block("5mg/片", specScore, 418, 300, 164), block("每日一次", .97, 660, 300, 180),
                block("医生：李某", .99, 80, 500, 250)));
    }

    @Test
    void shouldPrefillOnlyPrintedFieldsAndExcludeIdentity() {
        var blocks = table(.96, .98);
        blocks.addAll(List.of(block("二甲双胍片", .95, 90, 400, 220),
                block("0.5g/片", .99, 418, 400, 160), block("每日两次", .98, 650, 400, 180)));
        var result = parser.parse(blocks);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getDrugSpec()).isEqualTo("5mg/片");
        assertThat(result.getItems().get(1).getDrugSpec()).isEqualTo("0.5g/片");
        assertThat(result.getItems().get(1).getFrequency()).isEqualTo("每日两次");
        assertThat(result.getItems()).allSatisfy(item -> {
            assertThat(item.getDosage()).isNull();
            assertThat(item.getRoute()).isNull();
            assertThat(item.getDurationDays()).isNull();
        });
        assertThat(result.getConfidence()).isEqualByComparingTo(new BigDecimal("0.95"));
        assertThat(result.getExtractedText()).doesNotContain("张某", "李某", "患者", "医生");
        assertThat(result.getResponseJson()).doesNotContain("张某", "李某", "box");
    }

    @Test
    void shouldLeaveLowConfidenceOptionalFieldsEmpty() {
        var result = parser.parse(table(.96, .89));
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getItems().get(0).getDrugSpec()).isNull();
        assertThat(result.getConfidence()).isEqualByComparingTo("0.96");
    }

    @Test
    void shouldRequireManualReviewForLowConfidenceName() {
        var result = parser.parse(table(.89, .99));
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getExtractedText()).contains("氨氯地平片").doesNotContain("张某");
        assertThat(result.getFailureReason()).isEqualTo("OCR_MEDICATION_LOW_CONFIDENCE");
    }

    @Test
    void shouldRejectMergedColumnsAndDuplicateNamesInsteadOfMixingRows() {
        var blocks = table(.99, .99);
        blocks.add(block("另一药名", .99, 100, 310, 210));
        assertThat(parser.parse(blocks).getItems()).isEmpty();
        blocks = table(.99, .99);
        blocks.set(5, block("5mg/片 每日一次", .99, 418, 300, 400));
        assertThat(parser.parse(blocks).getItems()).isEmpty();
    }

    @Test
    void shouldNotAssociateFieldsFromDifferentRows() {
        var blocks = table(.99, .99);
        blocks.set(6, block("每日一次", .99, 660, 360, 180));
        assertThat(parser.parse(blocks).getItems().get(0).getFrequency()).isNull();
    }

    @Test
    void shouldSupportExplicitLabelsWithoutInferringMissingFields() {
        var result = parser.parse(List.of(block("药名：二甲双胍片；规格：0.5g/片；用法：每日两次", .97, 50, 100, 850)));
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getItems().get(0).getDrugName()).isEqualTo("二甲双胍片");
        assertThat(result.getItems().get(0).getDrugSpec()).isEqualTo("0.5g/片");
        assertThat(result.getItems().get(0).getFrequency()).isEqualTo("每日两次");
        assertThat(result.getItems().get(0).getDosage()).isNull();
    }

    @Test
    void shouldKeepNonPrescriptionAndUnknownLayoutsOutOfStorage() {
        var result = parser.parse(List.of(block("患者：张某 身份证：110101199001011234", .99, 10, 10, 800)));
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getExtractedText()).isNull();
        assertThat(result.getResponseJson()).isNull();
        assertThat(parser.parse(List.of()).getFailureReason()).isEqualTo("OCR_NO_TEXT");
    }

    @Test
    void shouldRejectInvalidCoordinatesAndScores() {
        assertThat(parser.parse(List.of(block("药名", Double.NaN, 10, 10, 100))).getFailureReason())
                .isEqualTo("OCR_RESPONSE_INVALID");
    }
}
