package com.sqlapk.medicine.module.patient.controller.admin.answers.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问卷 Excel VO
 *
 * @author 管理员
 */
@Data
public class AnswersExcelVO {

    @ExcelProperty("答案ID")
    private Long id;

    @ExcelProperty("用户ID")
    private Long userId;

    @ExcelProperty("问题ID")
    private Long questionId;

    @ExcelProperty("选项ID")
    private Long optionId;

    @ExcelProperty("答案值")
    private Integer value;

    @ExcelProperty("内容")
    private String content;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
