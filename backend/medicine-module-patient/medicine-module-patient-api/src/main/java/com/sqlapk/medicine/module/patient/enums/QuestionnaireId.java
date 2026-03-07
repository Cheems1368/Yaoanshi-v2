package com.sqlapk.medicine.module.patient.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum QuestionnaireId {
    CHRONIC_DISEASE(1, "慢性病知晓调查"),
    SATISFACTION(2, "满意度调查"),
    COMPLIANCE(3,"用药依从性调查"),
    PATIENT_INFO(4,"患者基础信息");
    private final int value;
    private final String name;

    public static String getQuestionnaireNameByValue(int value) {
        Optional<QuestionnaireId> first = Arrays.stream(QuestionnaireId.values()).filter(item -> item.getValue() == value).findFirst();
        if (first.isPresent()) {
            return first.get().getName();
        }
        return "";
    }
}
