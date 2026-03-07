package com.sqlapk.medicine.module.medication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用药计划状态枚举
 *
 * @author sqlapk
 */
@AllArgsConstructor
@Getter
public enum MedPlanStatus {

    DRAFT(0, "草稿"),
    ACTIVE(1, "活跃"),
    PAUSED(2, "暂停"),
    TERMINATED(3, "终止"),
    COMPLETED(4, "完成");

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
    public static MedPlanStatus valueOf(Integer status) {
        for (MedPlanStatus value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

}
