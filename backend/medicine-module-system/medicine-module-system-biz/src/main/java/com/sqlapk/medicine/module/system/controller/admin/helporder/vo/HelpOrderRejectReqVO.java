package com.sqlapk.medicine.module.system.controller.admin.helporder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 工单驳回请求VO
 */
@Data
@Schema(description = "管理后台 - 工单驳回请求")
public class HelpOrderRejectReqVO {

    @Schema(description = "工单ID", required = true, example = "1024")
    @NotNull(message = "工单ID不能为空")
    private Long id;

    @Schema(description = "驳回原因", required = true, example = "患者信息不完整")
    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 500, message = "驳回原因长度不能超过500个字符")
    private String reason;
}