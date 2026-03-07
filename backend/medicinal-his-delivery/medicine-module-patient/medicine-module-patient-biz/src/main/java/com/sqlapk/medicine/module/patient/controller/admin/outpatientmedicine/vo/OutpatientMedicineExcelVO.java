package com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药学门诊 Excel VO
 *
 * @author 管理员
 */
@Data
public class OutpatientMedicineExcelVO {

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("咨询ID")
    private Long id;

    @ExcelProperty("门诊药学ID")
    private Long outpatientId;

    @ExcelProperty("药学门诊ID")
    private Long meicineId;

}
