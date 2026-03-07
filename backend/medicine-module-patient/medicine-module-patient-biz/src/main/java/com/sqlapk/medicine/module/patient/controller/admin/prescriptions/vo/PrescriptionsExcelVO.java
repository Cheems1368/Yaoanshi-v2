package com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处方 Excel VO
 *
 * @author 管理员
 */
@Data
public class PrescriptionsExcelVO {

    @ExcelProperty("处方ID")
    private Long id;

    @ExcelProperty("患者ID")
    private Long patientId;

    @ExcelProperty("检查日期")
    private LocalDateTime createTime;

    @ExcelProperty("处方类型(门诊,住院,居家,药学)")
    private Integer type;

    @ExcelProperty("医生")
    private Long doctorId;

    @ExcelProperty("初步诊断")
    private String diagnosis;

}
