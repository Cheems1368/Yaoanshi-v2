package com.sqlapk.medicine.module.patient.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 患者档案审核状态枚举
 */
@AllArgsConstructor
@Getter
public enum PatientAuditStatus {

    PENDING("待审核", 0),
    APPROVED("已通过", 1),
    REJECTED("已拒绝", 2),
    NEEDS_REVISION("需要修改", 3);

    private final String description;
    private final Integer code;

    /**
     * 根据代码获取枚举
     */
    public static PatientAuditStatus fromCode(Integer code) {
        for (PatientAuditStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 检查是否为最终状态
     */
    public boolean isFinalStatus() {
        return this == APPROVED || this == REJECTED;
    }

    /**
     * 检查是否可以修改
     */
    public boolean canModify() {
        return this == PENDING || this == NEEDS_REVISION;
    }

}
