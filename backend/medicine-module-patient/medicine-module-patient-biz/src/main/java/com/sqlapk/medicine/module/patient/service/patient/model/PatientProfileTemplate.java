package com.sqlapk.medicine.module.patient.service.patient.model;

import com.sqlapk.medicine.module.patient.enums.TemplateType;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 患者档案模板
 */
@Data
public class PatientProfileTemplate {

    /**
     * 模板ID
     */
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
     * 模板类型
     */
    private TemplateType templateType;

    /**
     * 字段配置
     */
    private List<FieldConfig> fieldConfigs;

    /**
     * 是否默认模板
     */
    private Boolean isDefault;

    /**
     * 获取必填字段配置
     */
    public List<FieldConfig> getRequiredFields() {
        return fieldConfigs.stream()
                .filter(FieldConfig::getRequired)
                .collect(Collectors.toList());
    }

    /**
     * 获取重要字段配置
     */
    public List<FieldConfig> getImportantFields() {
        return fieldConfigs.stream()
                .filter(config -> config.getFieldLevel() != null && 
                        config.getFieldLevel().isImportant())
                .collect(Collectors.toList());
    }

    /**
     * 获取可选字段配置
     */
    public List<FieldConfig> getOptionalFields() {
        return fieldConfigs.stream()
                .filter(config -> config.getFieldLevel() != null && 
                        config.getFieldLevel().isOptional())
                .collect(Collectors.toList());
    }

    /**
     * 根据字段名获取字段配置
     */
    public FieldConfig getFieldConfig(String fieldName) {
        return fieldConfigs.stream()
                .filter(config -> fieldName.equals(config.getFieldName()))
                .findFirst()
                .orElse(null);
    }

}
