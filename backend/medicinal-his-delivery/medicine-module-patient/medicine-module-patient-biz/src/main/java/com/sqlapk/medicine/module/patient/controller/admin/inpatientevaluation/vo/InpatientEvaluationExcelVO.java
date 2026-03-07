package com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 住院药学用药评估 Excel VO
 *
 * @author 管理员
 */
@Data
public class InpatientEvaluationExcelVO {

    @ExcelProperty("记录ID")
    private Long id;

    @ExcelProperty("住院药学ID")
    private Long inpatientId;

    @ExcelProperty("日期")
    private LocalDate evaluationDate;

    @ExcelProperty("目标药物")
    private String targetDrug;

    @ExcelProperty("症状")
    private String symptom;

    @ExcelProperty("结果")
    private String result;

    @ExcelProperty("用药建议")
    private String medicationSuggestion;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
