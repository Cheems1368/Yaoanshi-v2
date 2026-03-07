package com.sqlapk.medicine.module.patient.controller.admin.questions.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 问卷问题更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuestionsUpdateReqVO extends QuestionsBaseVO {

    @Schema(description = "问题ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28039")
    @NotNull(message = "问题ID不能为空")
    private Long id;

}
