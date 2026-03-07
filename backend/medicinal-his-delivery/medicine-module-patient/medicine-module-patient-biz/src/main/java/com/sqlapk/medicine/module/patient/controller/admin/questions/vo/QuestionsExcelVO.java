package com.sqlapk.medicine.module.patient.controller.admin.questions.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 问卷问题 Excel VO
 *
 * @author 管理员
 */
@Data
public class QuestionsExcelVO {

    @ExcelProperty("问题ID")
    private Long id;

    @ExcelProperty("问卷内容")
    private String content;

    @ExcelProperty("问卷ID")
    private Long surveyId;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
