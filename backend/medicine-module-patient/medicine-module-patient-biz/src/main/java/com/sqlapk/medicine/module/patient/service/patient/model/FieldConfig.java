package com.sqlapk.medicine.module.patient.service.patient.model;

import com.sqlapk.medicine.module.patient.enums.FieldLevel;
import lombok.Data;

/**
 * 患者档案字段配置
 */
@Data
public class FieldConfig {

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段标签
     */
    private String fieldLabel;

    /**
     * 字段类型
     */
    private String fieldType;

    /**
     * 是否必填
     */
    private Boolean required;

    /**
     * 字段层级
     */
    private FieldLevel fieldLevel;

    /**
     * 验证规则
     */
    private String validationRule;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 字段描述
     */
    private String description;

    /**
     * 是否显示
     */
    private Boolean visible;

    /**
     * 是否可编辑
     */
    private Boolean editable;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

}
