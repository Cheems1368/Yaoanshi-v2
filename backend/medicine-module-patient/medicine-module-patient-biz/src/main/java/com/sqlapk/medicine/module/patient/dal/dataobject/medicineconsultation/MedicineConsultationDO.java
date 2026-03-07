package com.sqlapk.medicine.module.patient.dal.dataobject.medicineconsultation;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用药咨询 DO
 *
 * @author 管理员
 */
@TableName("patient_medicine_consultation")
@KeySequence("patient_medicine_consultation_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineConsultationDO extends BaseDO {

    /**
     * 咨询ID
     */
    @TableId
    private Long id;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 咨询内容
     */
    private String question;
    /**
     * 回答内容
     */
    private String answer;
    /**
     * 咨询对象(0 患者, 1 医护人员）
     *
     * 枚举 {@link TODO consultation_object 对应的类}
     */
    private Byte consultObject;
    /**
     * 特殊人群 (0 否, 哺乳期 1, 妊娠期 2)
     *
     * 枚举 {@link TODO special_patient 对应的类}
     */
    private Byte specialPatient;
    /**
     * 咨询方式(0面对面 1电话 2 互联网）
     *
     * 枚举 {@link TODO consultation_type 对应的类}
     */
    private Byte type;
    /**
     * 咨询时长
     */
    private Integer duration;
    /**
     * 咨询医师
     */
    private String doctorId;
    /**
     * 回答依据
     */
    private String answerBasis;
    /**
     * 备注
     */
    private String comment;
    /**
     * 咨询药物
     */
    private Long medicineId;

}
