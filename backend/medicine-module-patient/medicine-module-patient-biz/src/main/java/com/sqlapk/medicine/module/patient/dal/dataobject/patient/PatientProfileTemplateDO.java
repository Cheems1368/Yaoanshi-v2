package com.sqlapk.medicine.module.patient.dal.dataobject.patient;

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
 * 患者档案模板 DO
 *
 * @author 管理员
 */
@TableName("patient_profile_template")
@KeySequence("patient_profile_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfileTemplateDO extends BaseDO {

    /**
     * 模板ID
     */
    @TableId
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板类型（住院(0)、门诊(1)、居家(2)）
     */
    private Integer templateType;

    /**
     * 字段配置（JSON格式）
     */
    private String fieldConfigs;

    /**
     * 是否默认模板
     */
    private Boolean isDefault;

}
