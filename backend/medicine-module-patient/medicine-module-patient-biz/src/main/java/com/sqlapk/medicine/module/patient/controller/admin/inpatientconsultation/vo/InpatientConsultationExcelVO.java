package com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 主要用药咨询 Excel VO
 *
 * @author 管理员
 */
@Data
public class InpatientConsultationExcelVO {

    @ExcelProperty("咨询ID")
    private Long id;

    @ExcelProperty("住院药学ID")
    private Long inpatientId;

    @ExcelProperty("日期")
    private LocalDate consultationDate;

    @ExcelProperty("咨询要点")
    private String consultationPoints;

    @ExcelProperty("答复内容")
    private String replyContent;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
