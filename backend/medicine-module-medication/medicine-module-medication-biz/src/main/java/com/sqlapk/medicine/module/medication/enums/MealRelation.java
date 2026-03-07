package com.sqlapk.medicine.module.medication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 餐前后关系枚举
 *
 * @author sqlapk
 */
@AllArgsConstructor
@Getter
public enum MealRelation {

    INDEPENDENT(0, "独立"),
    BEFORE_MEAL(1, "餐前"),
    AFTER_MEAL(2, "餐后"),
    WITH_MEAL(3, "餐中");

    /**
     * 关系值
     */
    private final Integer relation;
    
    /**
     * 关系名称
     */
    private final String name;

    /**
     * 根据关系值获取枚举
     */
    public static MealRelation valueOf(Integer relation) {
        for (MealRelation value : values()) {
            if (value.getRelation().equals(relation)) {
                return value;
            }
        }
        return null;
    }

}
