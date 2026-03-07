package com.sqlapk.medicine.module.medication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报告状态枚举
 *
 * @author sqlapk
 */
@AllArgsConstructor
@Getter
public enum ReportStatus {

    GENERATING(1, "生成中"),
    COMPLETED(2, "已完成"),
    FAILED(3, "生成失败");

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
    public static ReportStatus valueOf(Integer status) {
        for (ReportStatus value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

}
