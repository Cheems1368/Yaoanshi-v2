package com.sqlapk.medicine.module.patient.controller.admin.examinations.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 检查项目 Excel VO
 *
 * @author 管理员
 */
@Data
public class ExaminationsExcelVO {

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("检查项目名称")
    private String name;

    @ExcelProperty("价格")
    private BigDecimal price;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("创建日期")
    private LocalDateTime createTime;

}
