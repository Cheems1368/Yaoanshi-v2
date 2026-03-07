package com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.sqlapk.medicine.framework.excel.core.annotations.DictFormat;
import com.sqlapk.medicine.framework.excel.core.convert.DictConvert;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 用药咨询 Excel VO
 *
 * @author 管理员
 */
@Data
public class MedicineConsultationExcelVO {

    @ExcelProperty("咨询ID")
    private Long id;

    @ExcelProperty("患者ID")
    private Long patientId;

    @ExcelProperty("咨询内容")
    private String question;

    @ExcelProperty("回答内容")
    private String answer;

    @ExcelProperty(value = "咨询对象(0 患者, 1 医护人员）", converter = DictConvert.class)
    @DictFormat("consultation_object") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Byte consultObject;

    @ExcelProperty(value = "特殊人群 (0 否, 哺乳期 1, 妊娠期 2)", converter = DictConvert.class)
    @DictFormat("special_patient") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Byte specialPatient;

    @ExcelProperty(value = "咨询方式(0面对面 1电话 2 互联网）", converter = DictConvert.class)
    @DictFormat("consultation_type") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Byte type;

    @ExcelProperty("咨询时长")
    private Integer duration;

    @ExcelProperty("咨询医师")
    private String doctorId;

    @ExcelProperty("回答依据")
    private String answerBasis;

    @ExcelProperty("备注")
    private String comment;

    @ExcelProperty("咨询日期")
    private LocalDateTime createTime;

    @ExcelProperty("咨询药物")
    private Long medicineId;

}
