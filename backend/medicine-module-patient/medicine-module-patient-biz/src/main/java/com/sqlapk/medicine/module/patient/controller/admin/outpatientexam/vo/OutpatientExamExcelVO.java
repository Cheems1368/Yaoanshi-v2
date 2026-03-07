package com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门诊药学检查项目明细 Excel VO
 *
 * @author 管理员
 */
@Data
public class OutpatientExamExcelVO {

    @ExcelProperty("咨询ID")
    private Long id;

    @ExcelProperty("门诊药学ID")
    private Long outpatientId;

    @ExcelProperty("检查项目ID")
    private Long examinationsId;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
