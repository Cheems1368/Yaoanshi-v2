package com.sqlapk.medicine.module.patient.controller.admin.answers.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 问卷更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AnswersUpdateReqVO extends AnswersBaseVO {

    @Schema(description = "答案ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7671")
    @NotNull(message = "答案ID不能为空")
    private Long id;

}
