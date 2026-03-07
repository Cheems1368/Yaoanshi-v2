package com.sqlapk.medicine.module.patient.controller.admin.surveys.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 问卷更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SurveysUpdateReqVO extends SurveysBaseVO {

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23190")
    @NotNull(message = "问卷ID不能为空")
    private Long id;

}
