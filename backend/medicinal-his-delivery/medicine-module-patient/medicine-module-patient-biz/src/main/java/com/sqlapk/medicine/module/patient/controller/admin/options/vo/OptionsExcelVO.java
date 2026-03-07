package com.sqlapk.medicine.module.patient.controller.admin.options.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 问题选项 Excel VO
 *
 * @author 管理员
 */
@Data
public class OptionsExcelVO {

    @ExcelProperty("选项ID")
    private Long id;

    @ExcelProperty("选项内容")
    private String content;

    @ExcelProperty("选项值")
    private Integer value;

    @ExcelProperty("选项分值")
    private Integer score;

    @ExcelProperty("问题ID")
    private Long questionId;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
