package com.sqlapk.medicine.module.patient.service.patient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientProfileTemplateDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.patient.PatientProfileTemplateMapper;
import com.sqlapk.medicine.module.patient.enums.TemplateType;
import com.sqlapk.medicine.module.patient.enums.FieldLevel;
import com.sqlapk.medicine.module.patient.service.patient.model.FieldConfig;
import com.sqlapk.medicine.module.patient.service.patient.model.PatientProfileTemplate;
import com.sqlapk.medicine.module.patient.service.patient.model.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 患者档案模板服务
 */
@Slf4j
@Service
public class PatientProfileTemplateService {

    @Autowired
    private PatientProfileTemplateMapper patientProfileTemplateMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取患者类型对应的模板
     */
    public PatientProfileTemplate getTemplateByPatientType(Short patientType) {
        TemplateType templateType = TemplateType.fromPatientType(patientType);
        return getDefaultTemplate(templateType);
    }

    /**
     * 获取默认模板
     */
    public PatientProfileTemplate getDefaultTemplate(TemplateType templateType) {
        PatientProfileTemplateDO templateDO = patientProfileTemplateMapper.selectDefaultByType(templateType.getCode());
        if (templateDO == null) {
            // 如果数据库中没有模板，返回内置模板
            return getBuiltinTemplate(templateType);
        }
        return convertToTemplate(templateDO);
    }

    /**
     * 验证患者档案是否符合模板要求
     */
    public ValidationResult validateAgainstTemplate(PatientsDO patient, 
                                                  PatientProfileTemplate template) {
        ValidationResult result = new ValidationResult();

        for (FieldConfig fieldConfig : template.getFieldConfigs()) {
            if (fieldConfig.getRequired()) {
                Object fieldValue = getFieldValue(patient, fieldConfig.getFieldName());
                if (fieldValue == null || !StringUtils.hasText(fieldValue.toString())) {
                    result.addError(fieldConfig.getFieldLabel() + "不能为空");
                }
            }
        }

        return result;
    }

    /**
     * 获取内置模板
     */
    private PatientProfileTemplate getBuiltinTemplate(TemplateType templateType) {
        PatientProfileTemplate template = new PatientProfileTemplate();
        template.setTemplateName(templateType.getDescription() + "档案模板");
        template.setDescription("内置" + templateType.getDescription() + "档案模板");
        template.setTemplateType(templateType);
        template.setIsDefault(true);

        // 设置字段配置
        List<FieldConfig> fieldConfigs = getDefaultFieldConfigs(templateType);
        template.setFieldConfigs(fieldConfigs);

        return template;
    }

    /**
     * 获取默认字段配置
     */
    private List<FieldConfig> getDefaultFieldConfigs(TemplateType templateType) {
        List<FieldConfig> configs = List.of(
                createFieldConfig("name", "患者姓名", "String", true, FieldLevel.REQUIRED),
                createFieldConfig("gender", "患者性别", "Integer", true, FieldLevel.REQUIRED),
                createFieldConfig("birthday", "患者生日", "LocalDateTime", true, FieldLevel.REQUIRED),
                createFieldConfig("mobile", "患者手机号", "String", true, FieldLevel.REQUIRED),
                createFieldConfig("idCard", "患者身份证号", "String", true, FieldLevel.REQUIRED),
                createFieldConfig("patientType", "患者类型", "Short", true, FieldLevel.REQUIRED),
                createFieldConfig("address", "家庭住址", "String", true, FieldLevel.REQUIRED),
                createFieldConfig("allergyHistory", "过敏史", "String", true, FieldLevel.REQUIRED),
                createFieldConfig("medicalHistory", "患病史", "String", true, FieldLevel.REQUIRED),
                createFieldConfig("familyHistory", "家族史", "String", false, FieldLevel.IMPORTANT),
                createFieldConfig("personalHistory", "个人史", "String", false, FieldLevel.IMPORTANT),
                createFieldConfig("insuranceCardNumber", "医保卡号", "String", false, FieldLevel.IMPORTANT),
                createFieldConfig("insurance", "报销类型", "Short", false, FieldLevel.IMPORTANT),
                createFieldConfig("emergencyContact", "紧急联系人", "String", false, FieldLevel.OPTIONAL),
                createFieldConfig("emergencyPhone", "紧急联系电话", "String", false, FieldLevel.OPTIONAL)
        );

        // 根据患者类型调整字段配置
        if (templateType == TemplateType.HOME_CARE) {
            // 居家患者紧急联系人是必填
            configs.stream()
                    .filter(config -> "emergencyContact".equals(config.getFieldName()) || 
                                    "emergencyPhone".equals(config.getFieldName()))
                    .forEach(config -> {
                        config.setRequired(true);
                        config.setFieldLevel(FieldLevel.REQUIRED);
                    });
        }

        return configs;
    }

    /**
     * 创建字段配置
     */
    private FieldConfig createFieldConfig(String fieldName, String fieldLabel, 
                                        String fieldType, boolean required, FieldLevel fieldLevel) {
        FieldConfig config = new FieldConfig();
        config.setFieldName(fieldName);
        config.setFieldLabel(fieldLabel);
        config.setFieldType(fieldType);
        config.setRequired(required);
        config.setFieldLevel(fieldLevel);
        config.setVisible(true);
        config.setEditable(true);
        return config;
    }

    /**
     * 转换数据库对象为模板对象
     */
    private PatientProfileTemplate convertToTemplate(PatientProfileTemplateDO templateDO) {
        PatientProfileTemplate template = new PatientProfileTemplate();
        template.setId(templateDO.getId());
        template.setTemplateName(templateDO.getTemplateName());
        template.setDescription(templateDO.getDescription());
        template.setTemplateType(TemplateType.fromCode(templateDO.getTemplateType()));
        template.setIsDefault(templateDO.getIsDefault());

        try {
            List<FieldConfig> fieldConfigs = objectMapper.readValue(
                    templateDO.getFieldConfigs(), 
                    new TypeReference<List<FieldConfig>>() {}
            );
            template.setFieldConfigs(fieldConfigs);
        } catch (Exception e) {
            log.error("解析字段配置失败: {}", e.getMessage());
            template.setFieldConfigs(getDefaultFieldConfigs(template.getTemplateType()));
        }

        return template;
    }

    /**
     * 获取字段值
     */
    private Object getFieldValue(PatientsDO patient, String fieldName) {
        try {
            Field field = PatientsDO.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(patient);
        } catch (Exception e) {
            log.error("获取字段值失败，字段名: {}, 错误: {}", fieldName, e.getMessage());
            return null;
        }
    }

}
