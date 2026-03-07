package com.sqlapk.medicine.module.system.controller.admin.helporder.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 工单 Excel 导出 VO")
@Data
@ExcelIgnoreUnannotated
public class HelpOrderExcelVO {

    @ExcelProperty("工单编号")
    private Long id;

    @ExcelProperty("患者编号")
    private Long patientId;

    @ExcelProperty("分配药师编号")
    private Long assignedPharmacistId;

    @ExcelProperty("分配医生编号")
    private Long assignedDoctorId;

    @ExcelProperty("工单标题")
    private String title;

    @ExcelProperty("工单描述")
    private String description;

    @ExcelProperty("意向药品")
    private String intendedDrugs;

    @ExcelProperty("附件列表")
    private String attachments;

    @ExcelProperty("是否需要医生")
    private Boolean toDoctor;

    @ExcelProperty("工单状态")
    private String status;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;
}
