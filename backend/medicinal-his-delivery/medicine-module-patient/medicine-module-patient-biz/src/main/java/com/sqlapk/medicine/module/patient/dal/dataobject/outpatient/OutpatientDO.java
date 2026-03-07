package com.sqlapk.medicine.module.patient.dal.dataobject.outpatient;

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
 * 门诊药学服务 DO
 *
 * @author 管理员
 */
@TableName("patient_outpatient")
@KeySequence("patient_outpatient_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutpatientDO extends BaseDO {

    /**
     * 门诊服务ID
     */
    @TableId
    private Long id;
    /**
     * 医生ID
     */
    private Long doctorId;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 过敏史
     */
    private String allergyHistory;
    /**
     * 患病史
     */
    private String medicalHistory;
    /**
     * 家族史
     */
    private String familyHistory;
    /**
     * 个人史
     */
    private String personalHistory;
    /**
     * 备注
     */
    private String comment;

}
