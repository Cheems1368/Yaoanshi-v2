package com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 居家用药教育更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HomeEducationUpdateReqVO extends HomeEducationBaseVO {

    @Schema(description = "教育记录", requiredMode = Schema.RequiredMode.REQUIRED, example = "22980")
    @NotNull(message = "教育记录不能为空")
    private Long id;

}
