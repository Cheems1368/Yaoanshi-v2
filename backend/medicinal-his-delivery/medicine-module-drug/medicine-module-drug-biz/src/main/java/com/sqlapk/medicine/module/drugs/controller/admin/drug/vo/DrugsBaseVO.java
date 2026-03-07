package com.sqlapk.medicine.module.drugs.controller.admin.drug.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 药品 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class DrugsBaseVO {

    @Schema(description = "药品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotNull(message = "药品名称不能为空")
    private String drugName;

    @Schema(description = "药品规格", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "药品规格不能为空")
    private String specification;

    @Schema(description = "生产厂家", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "生产厂家不能为空")
    private String manufacturer;

    @Schema(description = "批准文号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "批准文号不能为空")
    private String approvalNumber;

    @Schema(description = "有效期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "有效期不能为空")
    private LocalDate expiryDate;

    @Schema(description = "药品分类", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "药品分类不能为空")
    private String category;

    @Schema(description = "药品剂型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "药品剂型不能为空")
    private String dosageForm;

    @Schema(description = "药品成分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "药品成分不能为空")
    private String ingredients;

    @Schema(description = "适应症", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "适应症不能为空")
    private String indications;

    @Schema(description = "用法用量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用法用量不能为空")
    private String dosage;

    @Schema(description = "不良反应", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "不良反应不能为空")
    private String sideEffects;

    @Schema(description = "禁忌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "禁忌不能为空")
    private String contraindications;

    @Schema(description = "注意事项", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "注意事项不能为空")
    private String precautions;

    @Schema(description = "药物相互作用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "药物相互作用不能为空")
    private String interactions;

    @Schema(description = "药品图片URL", example = "https://www.iocoder.cn")
    private String imageUrl;

}
