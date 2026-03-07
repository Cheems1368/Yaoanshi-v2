package com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 居家随访评估 Excel VO
 *
 * @author 管理员
 */
@Data
public class HomeEvaluationExcelVO {

    @ExcelProperty("教育记录")
    private Long id;

    @ExcelProperty("日期")
    private LocalDate evaluationDate;

    @ExcelProperty("住院药学ID")
    private Long homeId;

    @ExcelProperty("日期")
    private String symptom;

    @ExcelProperty("检查指标")
    private String indicator;

    @ExcelProperty("治疗问题")
    private String treatmentProblem;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
