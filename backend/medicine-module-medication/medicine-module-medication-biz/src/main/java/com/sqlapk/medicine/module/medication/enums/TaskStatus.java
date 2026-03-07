package com.sqlapk.medicine.module.medication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态枚举
 *
 * @author sqlapk
 */
@AllArgsConstructor
@Getter
public enum TaskStatus {

    PENDING(0, "待完成"),
    COMPLETED(1, "已完成"),
    SKIPPED(2, "已跳过");

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
    public static TaskStatus valueOf(Integer status) {
        for (TaskStatus value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

}
