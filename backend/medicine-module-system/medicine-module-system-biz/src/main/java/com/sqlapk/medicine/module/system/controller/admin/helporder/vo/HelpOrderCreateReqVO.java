package com.sqlapk.medicine.module.system.controller.admin.helporder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Schema(description = "管理后台 - 工单创建 Request VO")
@Data
public class HelpOrderCreateReqVO {

    @Schema(description = "患者编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    @NotNull(message = "患者编号不能为空")
    private Long patientId;

    @Schema(description = "工单标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "用药咨询")
    @NotBlank(message = "工单标题不能为空")
    @Size(max = 128, message = "工单标题长度不能超过128个字符")
    private String title;

    @Schema(description = "工单描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "我想咨询一下这个药的用法")
    @NotBlank(message = "工单描述不能为空")
    private String description;

    @Schema(description = "意向药品", example = "[\"阿司匹林\", \"布洛芬\"]")
    private List<String> intendedDrugs;

    @Schema(description = "附件列表", example = "[\"file1.jpg\", \"file2.pdf\"]")
    private List<String> attachments;

    @Schema(description = "是否需要医生", example = "false")
    private Boolean toDoctor = false;

}

