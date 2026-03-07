package com.sqlapk.medicine.module.patient.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 患者档案字段层级枚举
 */
@AllArgsConstructor
@Getter
public enum FieldLevel {

    REQUIRED("红层必填", 1),
    IMPORTANT("橙层重要", 2),
    OPTIONAL("绿层可选", 3);

    private final String description;
    private final Integer level;

    /**
     * 根据层级获取枚举
     */
    public static FieldLevel fromLevel(Integer level) {
        for (FieldLevel fieldLevel : values()) {
            if (fieldLevel.getLevel().equals(level)) {
                return fieldLevel;
            }
        }
        return null;
    }

    /**
     * 检查是否为必填字段
     */
    public boolean isRequired() {
        return this == REQUIRED;
    }

    /**
     * 检查是否为重要字段
     */
    public boolean isImportant() {
        return this == IMPORTANT;
    }

    /**
     * 检查是否为可选字段
     */
    public boolean isOptional() {
        return this == OPTIONAL;
    }

}
