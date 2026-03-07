package com.sqlapk.medicine.module.patient.controller.admin.patient.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.sqlapk.medicine.framework.excel.core.annotations.DictFormat;
import com.sqlapk.medicine.framework.excel.core.convert.DictConvert;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 患者 Excel VO
 *
 * @author 管理员
 */
@Data
public class PatientsExcelVO {

    @ExcelProperty("患者ID")
    private Long id;

    @ExcelProperty("用户ID")
    private Long userId;

    @ExcelProperty("家庭住址")
    private String address;

    @ExcelProperty("医保卡号")
    private String insuranceCardNumber;

    @ExcelProperty("过敏史")
    private String allergyHistory;

    @ExcelProperty("患病史")
    private String medicalHistory;

    @ExcelProperty("家族史")
    private String familyHistory;

    @ExcelProperty("个人史")
    private String personalHistory;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("生日")
    private LocalDateTime birthday;

    @ExcelProperty(value = "患者类型（住院(0)、门诊(1)、居家(2)）", converter = DictConvert.class)
    @DictFormat("patient_type") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Short patientType;

    @ExcelProperty(value = "报销类型（医保(0)、非医保(1)）", converter = DictConvert.class)
    @DictFormat("insurance") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Short insurance;

}
