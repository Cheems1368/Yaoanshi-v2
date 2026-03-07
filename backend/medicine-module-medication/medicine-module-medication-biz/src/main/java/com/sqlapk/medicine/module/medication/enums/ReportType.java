package com.sqlapk.medicine.module.medication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报告类型枚举
 *
 * @author sqlapk
 */
@AllArgsConstructor
@Getter
public enum ReportType {

    WEEKLY(1, "周报告", 7),
    MONTHLY(2, "月报告", 30),
    QUARTERLY(3, "季度报告", 90),
    YEARLY(4, "年度报告", 365);

    /**
     * 类型值
     */
    private final Integer type;
    
    /**
     * 类型名称
     */
    private final String name;
    
    /**
     * 默认天数
     */
    private final Integer defaultDays;

    /**
     * 根据类型值获取枚举
     */
    public static ReportType valueOf(Integer type) {
        for (ReportType value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

}
