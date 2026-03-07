package com.sqlapk.medicine.module.patient.controller.admin.education.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用药教育 Excel VO
 *
 * @author 管理员
 */
@Data
public class EducationExcelVO {

    @ExcelProperty("咨询ID")
    private Long id;

    @ExcelProperty("药品名称")
    private Long medicineName;

    @ExcelProperty("患者ID")
    private Long patientId;

    @ExcelProperty("疾病")
    private String disease;

    @ExcelProperty("教育内容")
    private String content;

    @ExcelProperty("教育依据")
    private String contentBasis;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
