package com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用药清单 Excel VO
 *
 * @author 管理员
 */
@Data
public class HomeMedicineExcelVO {

    @ExcelProperty("住院医嘱ID")
    private Long id;

    @ExcelProperty("居家药学ID")
    private Long homeId;

    @ExcelProperty("药品名称")
    private String medicineName;

    @ExcelProperty("医师ID")
    private Long doctorId;

    @ExcelProperty("处方医师")
    private String doctorName;

    @ExcelProperty("剂量")
    private String dosage;

    @ExcelProperty("适应症")
    private String indications;

    @ExcelProperty("用法")
    private String medicineUsage;

    @ExcelProperty("开始日期")
    private LocalDateTime startTime;

    @ExcelProperty("访视时间")
    private LocalDateTime endTime;

    @ExcelProperty("特殊说明")
    private String comment;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
