package com.sqlapk.medicine.module.medication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发送状态枚举
 *
 * @author sqlapk
 */
@AllArgsConstructor
@Getter
public enum SendStatus {

    PENDING(0, "待发送"),
    SENDING(1, "发送中"),
    SUCCESS(2, "发送成功"),
    FAILED(3, "发送失败");

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
    public static SendStatus valueOf(Integer status) {
        for (SendStatus value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

}
