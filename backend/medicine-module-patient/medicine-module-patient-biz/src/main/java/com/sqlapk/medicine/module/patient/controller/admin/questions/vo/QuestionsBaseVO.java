package com.sqlapk.medicine.module.patient.controller.admin.questions.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 问卷问题 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class QuestionsBaseVO {

    @Schema(description = "问卷内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "问卷内容不能为空")
    private String content;

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "18529")
    @NotNull(message = "问卷ID不能为空")
    private Long surveyId;

}
