package com.sqlapk.medicine.module.drugs.controller.admin.drug.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 药品 Excel 导出 Request VO，参数和 DrugsPageReqVO 是一致的")
@Data
public class DrugsExportReqVO {

    @Schema(description = "药品名称", example = "赵六")
    private String drugName;

    @Schema(description = "药品规格")
    private String specification;

    @Schema(description = "生产厂家")
    private String manufacturer;

    @Schema(description = "批准文号")
    private String approvalNumber;

    @Schema(description = "有效期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] expiryDate;

    @Schema(description = "药品分类")
    private String category;

    @Schema(description = "药品剂型")
    private String dosageForm;

    @Schema(description = "药品成分")
    private String ingredients;

    @Schema(description = "适应症")
    private String indications;

    @Schema(description = "用法用量")
    private String dosage;

    @Schema(description = "不良反应")
    private String sideEffects;

    @Schema(description = "禁忌")
    private String contraindications;

    @Schema(description = "注意事项")
    private String precautions;

    @Schema(description = "药物相互作用")
    private String interactions;

    @Schema(description = "药品图片URL", example = "https://www.iocoder.cn")
    private String imageUrl;

}
