package com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 患者处方 Excel VO
 *
 * @author 管理员
 */
@Data
public class PrescriptionDetailExcelVO {

    @ExcelProperty("处方ID")
    private Long id;

    @ExcelProperty("处方ID")
    private Long prescriptionId;

    @ExcelProperty("药品ID")
    private Long medicineId;

    @ExcelProperty("用量")
    private String dosage;

    @ExcelProperty("用法")
    private String usage;

    @ExcelProperty("频次")
    private String frequency;

    @ExcelProperty("数量")
    private Integer quality;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("检查日期")
    private LocalDateTime createTime;

}
