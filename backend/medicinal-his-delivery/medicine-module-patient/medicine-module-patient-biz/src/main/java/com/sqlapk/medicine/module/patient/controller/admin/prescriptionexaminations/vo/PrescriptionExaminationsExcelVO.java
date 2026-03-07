package com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处方检查项目关系 Excel VO
 *
 * @author 管理员
 */
@Data
public class PrescriptionExaminationsExcelVO {

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("处方ID")
    private Long prescriptionId;

    @ExcelProperty("检查项目ID")
    private Long examinationId;

    @ExcelProperty("检查结果")
    private String result;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
