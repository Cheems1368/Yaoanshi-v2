package com.sqlapk.medicine.module.medication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 *
 * @author sqlapk
 */
@AllArgsConstructor
@Getter
public enum NotificationType {

    MEDICATION_REMINDER(1, "用药提醒"),
    PLAN_CHANGE(2, "计划变更"),
    ABNORMAL_ALERT(3, "异常提醒"),
    REPORT_GENERATED(4, "报告生成");

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
    public static NotificationType valueOf(Integer type) {
        for (NotificationType value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

}
