package com.sqlapk.medicine.module.patient.controller.admin.surveys.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 问卷 Excel VO
 *
 * @author 管理员
 */
@Data
public class SurveysExcelVO {

    @ExcelProperty("问卷ID")
    private Long id;

    @ExcelProperty("标题")
    private String title;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
