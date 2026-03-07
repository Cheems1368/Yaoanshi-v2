package com.sqlapk.medicine.module.system.controller.admin.helporder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 工单升级请求VO
 */
@Data
@Schema(description = "管理后台 - 工单升级请求")
public class HelpOrderEscalateReqVO {

    @Schema(description = "工单ID", required = true, example = "1024")
    @NotNull(message = "工单ID不能为空")
    private Long id;

    @Schema(description = "是否需要医生", required = true)
    @NotNull(message = "是否需要医生不能为空")
    private Boolean needDoctor;

    @Schema(description = "医生ID", required = true, example = "1003")
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;
}