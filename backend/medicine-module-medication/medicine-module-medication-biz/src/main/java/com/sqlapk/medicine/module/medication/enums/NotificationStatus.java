package com.sqlapk.medicine.module.medication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知状态枚举
 *
 * @author sqlapk
 */
@AllArgsConstructor
@Getter
public enum NotificationStatus {

    UNREAD(0, "未读"),
    READ(1, "已读"),
    DELETED(2, "已删除");

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
    public static NotificationStatus valueOf(Integer status) {
        for (NotificationStatus value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

}
