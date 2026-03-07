package com.sqlapk.medicine.module.patient.dal.dataobject.education;

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
 * 用药教育 DO
 *
 * @author 管理员
 */
@TableName("patient_education")
@KeySequence("patient_education_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationDO extends BaseDO {

    /**
     * 咨询ID
     */
    @TableId
    private Long id;
    /**
     * 药品名称
     */
    private String medicineName;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 疾病
     */
    private String disease;
    /**
     * 教育内容
     */
    private String content;
    /**
     * 教育依据
     */
    private String contentBasis;

}
