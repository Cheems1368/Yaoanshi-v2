package com.medicine.service.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiPrivacyRedactorTest {

    private final AiPrivacyRedactor redactor = new AiPrivacyRedactor();

    @Test
    void shouldRedactHighConfidenceIdentifiers() {
        AiPrivacyRedactor.RedactionResult result = redactor.redact(
                "姓名：张三，手机号13812345678，身份证11010519491231002X，邮箱 patient@example.com，病历号：MR-2026-08"
        );

        assertThat(result.text())
                .contains("姓名：[姓名已隐藏]", "[手机号已隐藏]", "[身份证号已隐藏]", "[邮箱已隐藏]", "病历号：[编号已隐藏]")
                .doesNotContain("张三", "13812345678", "11010519491231002X", "patient@example.com", "MR-2026-08");
        assertThat(result.redactionCount()).isEqualTo(5);
    }

    @Test
    void shouldKeepMedicationDoseAndTimeIntact() {
        String text = "阿司匹林100mg每天1次，早上8:30服用，连续15天。";

        AiPrivacyRedactor.RedactionResult result = redactor.redact(text);

        assertThat(result.text()).isEqualTo(text);
        assertThat(result.redactionCount()).isZero();
    }

    @Test
    void shouldRedactEncounterAndPatientIdLabels() {
        AiPrivacyRedactor.RedactionResult result = redactor.redact(
                "就诊编号：VISIT-12345，就诊号：V-2026-08，患者ID：P-12345"
        );

        assertThat(result.text())
                .contains("就诊编号：[编号已隐藏]", "就诊号：[编号已隐藏]", "患者ID：[编号已隐藏]")
                .doesNotContain("VISIT-12345", "V-2026-08", "P-12345");
        assertThat(result.redactionCount()).isEqualTo(3);
    }
}
