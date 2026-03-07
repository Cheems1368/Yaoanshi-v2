package com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 住院药学查房记录 Excel VO
 *
 * @author 管理员
 */
@Data
public class InpatientRoundExcelVO {

    @ExcelProperty("查房id")
    private Long id;

    @ExcelProperty("住院药学ID")
    private Long inpatientId;

    @ExcelProperty("日期")
    private LocalDate roundDate;

    @ExcelProperty("查房内容")
    private String roundContent;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
