package com.sqlapk.medicine.module.patient.controller.admin.options.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 问题选项 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class OptionsBaseVO {

    @Schema(description = "选项内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "选项内容不能为空")
    private String content;

    @Schema(description = "选项值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "选项值不能为空")
    private Integer value;

    @Schema(description = "选项分值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "选项分值不能为空")
    private Integer score;

    @Schema(description = "问题ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8349")
    @NotNull(message = "问题ID不能为空")
    private Long questionId;

}
