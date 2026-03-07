package com.sqlapk.medicine.module.drugs.controller.admin.drug.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 药品 Excel VO
 *
 * @author 管理员
 */
@Data
public class DrugsExcelVO {

    @ExcelProperty("药品ID")
    private Long drugId;

    @ExcelProperty("药品名称")
    private String drugName;

    @ExcelProperty("药品规格")
    private String specification;

    @ExcelProperty("生产厂家")
    private String manufacturer;

    @ExcelProperty("批准文号")
    private String approvalNumber;

    @ExcelProperty("有效期")
    private LocalDate expiryDate;

    @ExcelProperty("药品分类")
    private String category;

    @ExcelProperty("药品剂型")
    private String dosageForm;

    @ExcelProperty("药品成分")
    private String ingredients;

    @ExcelProperty("适应症")
    private String indications;

    @ExcelProperty("用法用量")
    private String dosage;

    @ExcelProperty("不良反应")
    private String sideEffects;

    @ExcelProperty("禁忌")
    private String contraindications;

    @ExcelProperty("注意事项")
    private String precautions;

    @ExcelProperty("药物相互作用")
    private String interactions;

    @ExcelProperty("药品图片URL")
    private String imageUrl;

}
