package com.medicine.api.dto.assessment;

import java.util.List;

public record KnowledgeQuestionDTO(String questionCode, String questionText, List<OptionDTO> options) {
    public record OptionDTO(String code, String text) {
    }
}
