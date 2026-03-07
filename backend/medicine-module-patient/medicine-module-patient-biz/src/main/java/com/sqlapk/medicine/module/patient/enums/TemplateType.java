package com.sqlapk.medicine.module.patient.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 患者档案模板类型枚举
 */
@AllArgsConstructor
@Getter
public enum TemplateType {

    INPATIENT("住院患者", 0),
    OUTPATIENT("门诊患者", 1),
    HOME_CARE("居家患者", 2);

    private final String description;
    private final Integer code;

    /**
     * 根据代码获取枚举
     */
    public static TemplateType fromCode(Integer code) {
        for (TemplateType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据患者类型获取模板类型
     */
    public static TemplateType fromPatientType(Short patientType) {
        if (patientType == null) {
            return OUTPATIENT;
        }
        switch (patientType) {
            case 0: return INPATIENT;
            case 1: return OUTPATIENT;
            case 2: return HOME_CARE;
            default: return OUTPATIENT;
        }
    }

}
