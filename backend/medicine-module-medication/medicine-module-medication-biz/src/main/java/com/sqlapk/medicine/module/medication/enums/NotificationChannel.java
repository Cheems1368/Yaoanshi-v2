package com.sqlapk.medicine.module.medication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知渠道枚举
 *
 * @author sqlapk
 */
@AllArgsConstructor
@Getter
public enum NotificationChannel {

    IN_APP(1, "站内通知"),
    SMS(2, "短信"),
    VOICE(3, "语音"),
    EMAIL(4, "邮件"),
    MULTI_CHANNEL(5, "多渠道");

    /**
     * 渠道值
     */
    private final Integer channel;
    
    /**
     * 渠道名称
     */
    private final String name;

    /**
     * 根据渠道值获取枚举
     */
    public static NotificationChannel valueOf(Integer channel) {
        for (NotificationChannel value : values()) {
            if (value.getChannel().equals(channel)) {
                return value;
            }
        }
        return null;
    }

}
