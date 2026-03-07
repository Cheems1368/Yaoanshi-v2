package com.sqlapk.medicine.module.system.controller.admin.helporder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 工单分配请求VO
 */
@Data
@Schema(description = "管理后台 - 工单分配请求")
public class HelpOrderAssignReqVO {

    @Schema(description = "工单ID", required = true, example = "1024")
    @NotNull(message = "工单ID不能为空")
    private Long id;

    @Schema(description = "药师ID", required = true, example = "1001")
    @NotNull(message = "药师ID不能为空")
    private Long pharmacistId;
}