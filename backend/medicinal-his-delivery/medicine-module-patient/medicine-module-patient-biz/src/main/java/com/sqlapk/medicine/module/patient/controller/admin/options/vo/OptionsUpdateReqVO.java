package com.sqlapk.medicine.module.patient.controller.admin.options.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 问题选项更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OptionsUpdateReqVO extends OptionsBaseVO {

    @Schema(description = "选项ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1532")
    @NotNull(message = "选项ID不能为空")
    private Long id;

}
