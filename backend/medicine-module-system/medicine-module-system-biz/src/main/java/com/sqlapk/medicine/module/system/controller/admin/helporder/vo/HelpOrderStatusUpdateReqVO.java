package com.sqlapk.medicine.module.system.controller.admin.helporder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 工单状态更新请求VO
 */
@Data
@Schema(description = "管理后台 - 工单状态更新请求")
public class HelpOrderStatusUpdateReqVO {

    @Schema(description = "工单ID", required = true, example = "1024")
    @NotNull(message = "工单ID不能为空")
    private Long id;

    @Schema(description = "工单状态", required = true, example = "ASSIGNED_PHARMACIST")
    @NotBlank(message = "工单状态不能为空")
    private String status;

    @Schema(description = "状态更新原因", example = "开始处理工单")
    @Size(max = 500, message = "状态更新原因长度不能超过500个字符")
    private String reason;
}