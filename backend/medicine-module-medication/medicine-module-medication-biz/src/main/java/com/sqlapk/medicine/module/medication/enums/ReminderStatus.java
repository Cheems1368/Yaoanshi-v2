package com.sqlapk.medicine.module.medication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提醒状态枚举
 *
 * @author sqlapk
 */
@AllArgsConstructor
@Getter
public enum ReminderStatus {

    PENDING(0, "待执行"),
    EXECUTING(1, "执行中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败"),
    ABANDONED(4, "已放弃");

    /**
     * 状态值
     */
    private final Integer status;
    
    /**
     * 状态名称
     */
    private final String name;

    /**
     * 根据状态值获取枚举
     */
    public static ReminderStatus valueOf(Integer status) {
        for (ReminderStatus value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

}
