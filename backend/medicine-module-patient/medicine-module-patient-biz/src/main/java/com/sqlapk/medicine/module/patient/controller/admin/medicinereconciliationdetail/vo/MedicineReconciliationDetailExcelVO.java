package com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药物重整剂量 Excel VO
 *
 * @author 管理员
 */
@Data
public class MedicineReconciliationDetailExcelVO {

    @ExcelProperty("明细ID")
    private Long id;

    @ExcelProperty("重整ID")
    private Long reconciliationId;

    @ExcelProperty("药品名称")
    private String medicineName;

    @ExcelProperty("用法用量")
    private String dosage;

    @ExcelProperty("用药原因")
    private String usageReason;

    @ExcelProperty("开始日期")
    private LocalDateTime startTime;

    @ExcelProperty("访视时间")
    private LocalDateTime endTime;

    @ExcelProperty("备注,重整原因")
    private String comment;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
