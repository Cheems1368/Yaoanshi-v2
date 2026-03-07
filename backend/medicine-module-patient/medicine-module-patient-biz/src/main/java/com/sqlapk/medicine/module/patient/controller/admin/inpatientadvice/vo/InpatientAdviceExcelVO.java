package com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 住院药学不合理用药 Excel VO
 *
 * @author 管理员
 */
@Data
public class InpatientAdviceExcelVO {

    @ExcelProperty("住院医嘱ID")
    private Long id;

    @ExcelProperty("住院药学ID")
    private Long inpatientId;

    @ExcelProperty("日期")
    private LocalDate adviceDate;

    @ExcelProperty("医嘱内容")
    private String adviceContent;

    @ExcelProperty("不合理原因")
    private String irrationalReason;

    @ExcelProperty("用药建议")
    private String medicationSuggestion;

    @ExcelProperty("结果")
    private String result;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
