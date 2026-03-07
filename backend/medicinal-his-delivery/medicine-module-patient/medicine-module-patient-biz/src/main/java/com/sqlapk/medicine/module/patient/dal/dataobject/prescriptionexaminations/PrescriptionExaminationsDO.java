package com.sqlapk.medicine.module.patient.dal.dataobject.prescriptionexaminations;

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
 * 处方检查项目关系 DO
 *
 * @author 管理员
 */
@TableName("patient_prescription_examinations")
@KeySequence("patient_prescription_examinations_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionExaminationsDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 处方ID
     */
    private Long prescriptionId;
    /**
     * 检查项目ID
     */
    private Long examinationId;
    /**
     * 检查结果
     */
    private String result;

}
