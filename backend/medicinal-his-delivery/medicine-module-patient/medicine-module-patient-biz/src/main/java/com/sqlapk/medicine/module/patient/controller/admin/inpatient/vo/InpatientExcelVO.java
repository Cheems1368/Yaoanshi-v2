package com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 住院药学服务 Excel VO
 *
 * @author 管理员
 */
@Data
public class InpatientExcelVO {

    @ExcelProperty("住院服务ID")
    private Long id;

    @ExcelProperty("住院号")
    private String hospitalNumber;

    @ExcelProperty("床号")
    private String bedNumber;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("住院日期")
    private LocalDate admissionDate;

    @ExcelProperty("主诉诊断")
    private String diagnosis;

    @ExcelProperty("依从性评价")
    private String compliance;

    @ExcelProperty("患者病情")
    private String patientCondition;

    @ExcelProperty("特殊生理状态")
    private String specialPhysiologicalStatus;

    @ExcelProperty("调整方案")
    private String adjustmentPlan;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    private Long patientId;

    private Long doctorId;

}
