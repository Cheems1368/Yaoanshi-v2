package com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门诊处方用药教育明细 Excel VO
 *
 * @author 管理员
 */
@Data
public class OutpatientEducationExcelVO {

    @ExcelProperty("咨询ID")
    private Long id;

    @ExcelProperty("门诊药学ID")
    private Long outpatientId;

    @ExcelProperty("教育ID")
    private Long educationId;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
