package com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药学门诊 Excel VO
 *
 * @author 管理员
 */
@Data
public class PharmacyOutpatientExcelVO {

    @ExcelProperty("门诊ID")
    private Long id;

    @ExcelProperty("处方ID")
    private Long prescriptionId;

    @ExcelProperty("问题描述")
    private String medicineName;

    @ExcelProperty("用法用量")
    private String medicineUsage;

    @ExcelProperty("开始用药时间")
    private LocalDateTime startUsageTime;

    @ExcelProperty("停止用药时间")
    private LocalDateTime stopUsageTime;

    @ExcelProperty("停药原因")
    private String stopReason;

    @ExcelProperty("mrp 类别")
    private String mrpType;

    @ExcelProperty("权重")
    private String weight;

    @ExcelProperty("mrp 描述")
    private String mrpDesc;

    @ExcelProperty("检查日期")
    private LocalDateTime createTime;

}
