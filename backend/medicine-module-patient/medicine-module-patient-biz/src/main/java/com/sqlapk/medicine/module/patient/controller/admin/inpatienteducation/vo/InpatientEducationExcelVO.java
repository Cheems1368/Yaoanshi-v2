package com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 住院用药教育 Excel VO
 *
 * @author 管理员
 */
@Data
public class InpatientEducationExcelVO {

    @ExcelProperty("教育记录")
    private Long id;

    @ExcelProperty("住院药学ID")
    private Long inpatientId;

    @ExcelProperty("日期")
    private LocalDate educationDate;

    @ExcelProperty("诊断与用药")
    private String diagnosisAndMedication;

    @ExcelProperty("要点")
    private String keyPoints;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
