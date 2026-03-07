package com.sqlapk.medicine.module.patient.controller.admin.home.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 居家药学服务 Excel VO
 *
 * @author 管理员
 */
@Data
public class HomeExcelVO {

    @ExcelProperty("居家药学服务ID")
    private Long id;

    @ExcelProperty("医生ID")
    private Long doctorId;

    @ExcelProperty("患者ID")
    private Long patientId;

    @ExcelProperty("患者姓名")
    private String name;

    @ExcelProperty("医保卡号")
    private String insurance;

    @ExcelProperty("性别")
    private Integer gender;

    @ExcelProperty("创建时间")
    private LocalDateTime birthday;

    @ExcelProperty("家庭住址")
    private String address;

    @ExcelProperty("联系方式")
    private String phone;

    @ExcelProperty("访视地点")
    private String interviewLocation;

    @ExcelProperty("访视时间")
    private LocalDateTime interviewTime;

    @ExcelProperty("合并疾病")
    private String complicatedDisease;

    @ExcelProperty("访视目的")
    private String interviewPurpose;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
