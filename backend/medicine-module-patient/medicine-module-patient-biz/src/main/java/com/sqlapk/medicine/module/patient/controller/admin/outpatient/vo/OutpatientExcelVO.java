package com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门诊药学服务 Excel VO
 *
 * @author 管理员
 */
@Data
public class OutpatientExcelVO {

    @ExcelProperty("门诊服务ID")
    private Long id;

    @ExcelProperty("医生ID")
    private Long doctorId;

    @ExcelProperty("患者ID")
    private Long patientId;

    @ExcelProperty("过敏史")
    private String allergyHistory;

    @ExcelProperty("患病史")
    private String medicalHistory;

    @ExcelProperty("家族史")
    private String familyHistory;

    @ExcelProperty("个人史")
    private String personalHistory;

    @ExcelProperty("备注")
    private String comment;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
