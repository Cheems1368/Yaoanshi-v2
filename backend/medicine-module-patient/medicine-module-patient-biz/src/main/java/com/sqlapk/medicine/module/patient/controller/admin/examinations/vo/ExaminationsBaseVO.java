package com.sqlapk.medicine.module.patient.controller.admin.examinations.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 检查项目 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ExaminationsBaseVO {

    @Schema(description = "检查项目名称", example = "芋艿")
    private String name;

    @Schema(description = "价格", example = "17583")
    private BigDecimal price;

    @Schema(description = "描述", example = "随便")
    private String description;

}
