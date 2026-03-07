package com.sqlapk.medicine.module.medication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 打卡状态枚举
 *
 * @author sqlapk
 */
@AllArgsConstructor
@Getter
public enum CheckinStatus {

    COMPLETED(1, "已完成"),
    SKIPPED(2, "已跳过"),
    REVOKED(3, "已撤销");

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
    public static CheckinStatus valueOf(Integer status) {
        for (CheckinStatus value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

}
