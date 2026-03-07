package com.sqlapk.medicine.module.medication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 核实状态枚举
 *
 * @author sqlapk
 */
@AllArgsConstructor
@Getter
public enum VerifyStatus {

    PENDING(0, "未核实"),
    VERIFIED(1, "已核实"),
    FAILED(2, "核实失败");

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
    public static VerifyStatus valueOf(Integer status) {
        for (VerifyStatus value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

}
