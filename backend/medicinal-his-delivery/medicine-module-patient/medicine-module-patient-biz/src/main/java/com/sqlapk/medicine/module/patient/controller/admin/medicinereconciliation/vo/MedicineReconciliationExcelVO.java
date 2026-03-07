package com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药物重整剂量 Excel VO
 *
 * @author 管理员
 */
@Data
public class MedicineReconciliationExcelVO {

    @ExcelProperty("重整ID")
    private Long id;

    @ExcelProperty("患者ID")
    private Long patientId;

    @ExcelProperty("医生ID")
    private Long doctorId;

    @ExcelProperty("患者姓名")
    private String name;

    @ExcelProperty("性别")
    private Integer gender;

    @ExcelProperty("创建时间")
    private LocalDateTime birthday;

    @ExcelProperty("联系方式")
    private String phone;

    @ExcelProperty("入院/就诊时间")
    private LocalDateTime clinicTime;

    @ExcelProperty("出院/转科时间")
    private LocalDateTime dischargeTime;

    @ExcelProperty("主要诊断")
    private String diagnose;

    @ExcelProperty("过敏史")
    private String allergyHistory;

    @ExcelProperty("用药相关问题")
    private String medicineQuestion;

    @ExcelProperty("药师姓名")
    private String medicineDoctor;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
