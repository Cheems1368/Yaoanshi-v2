package com.sqlapk.medicine.module.medication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提醒类型枚举
 *
 * @author sqlapk
 */
@AllArgsConstructor
@Getter
public enum ReminderType {

    MEDICATION(1, "用药提醒"),
    FOLLOW_UP(2, "复诊提醒"),
    EXAMINATION(3, "检查提醒");

    /**
     * 类型值
     */
    private final Integer type;
    
    /**
     * 类型名称
     */
    private final String name;

    /**
     * 根据类型值获取枚举
     */
    public static ReminderType valueOf(Integer type) {
        for (ReminderType value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

}
