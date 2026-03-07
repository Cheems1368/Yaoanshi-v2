package com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处方审核 Excel VO
 *
 * @author 管理员
 */
@Data
public class OutpatientCheckExcelVO {

    @ExcelProperty("审核ID")
    private Long id;

    @ExcelProperty("门诊药学ID")
    private Long outpatientId;

    @ExcelProperty("不合理用药名称")
    private String medicineName;

    @ExcelProperty("处方医生")
    private Long doctorId;

    @ExcelProperty("处方ID")
    private Long prescriptionId;

    @ExcelProperty("问题描述")
    private String issueDesc;

    @ExcelProperty("问题代码")
    private String issueCode;

    @ExcelProperty("干预结果")
    private String interveneResult;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
