package com.sqlapk.medicine.module.system.controller.admin.helporder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 工单更新 Request VO")
@Data
public class HelpOrderUpdateReqVO {

    @Schema(description = "工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工单编号不能为空")
    private Long id;

    @Schema(description = "工单标题", example = "用药咨询")
    private String title;

    @Schema(description = "工单描述", example = "我想咨询一下这个药的用法")
    private String description;

    @Schema(description = "意向药品", example = "[\"阿司匹林\", \"布洛芬\"]")
    private List<String> intendedDrugs;

    @Schema(description = "附件列表", example = "[\"file1.jpg\", \"file2.pdf\"]")
    private List<String> attachments;

    @Schema(description = "是否需要医生", example = "false")
    private Boolean toDoctor;

}

