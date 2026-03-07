package com.sqlapk.medicine.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工单状态枚举
 *
 * @author sqlapk
 */
@Getter
@AllArgsConstructor
public enum HelpOrderStatus {

    SUBMITTED("SUBMITTED", "已提交"),
    ASSIGNED_PHARMACIST("ASSIGNED_PHARMACIST", "已分配药师"),
    PHARMACIST_REVIEW("PHARMACIST_REVIEW", "药师审核中"),
    ADVICE_READY("ADVICE_READY", "建议已就绪"),
    TO_DOCTOR("TO_DOCTOR", "转交医生"),
    DOCTOR_REVIEW("DOCTOR_REVIEW", "医生审核中"),
    PRESCRIPTION_READY("PRESCRIPTION_READY", "处方已就绪"),
    DONE("DONE", "已完成"),
    REJECTED("REJECTED", "已驳回");

    /**
     * 状态编码
     */
    private final String code;
    /**
     * 状态名称
     */
    private final String name;

    /**
     * 根据编码获取状态
     *
     * @param code 编码
     * @return 状态
     */
    public static HelpOrderStatus fromCode(String code) {
        for (HelpOrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }

    /**
     * 判断是否为初始状态
     *
     * @return 是否为初始状态
     */
    public boolean isInitial() {
        return this == SUBMITTED;
    }

    /**
     * 判断是否为药师处理状态
     *
     * @return 是否为药师处理状态
     */
    public boolean isPharmacistProcessing() {
        return this == ASSIGNED_PHARMACIST || this == PHARMACIST_REVIEW || this == ADVICE_READY;
    }

    /**
     * 判断是否为医生处理状态
     *
     * @return 是否为医生处理状态
     */
    public boolean isDoctorProcessing() {
        return this == TO_DOCTOR || this == DOCTOR_REVIEW || this == PRESCRIPTION_READY;
    }

    /**
     * 判断是否为完成状态
     *
     * @return 是否为完成状态
     */
    public boolean isCompleted() {
        return this == DONE || this == REJECTED;
    }

    /**
     * 判断是否可以分配药师
     *
     * @return 是否可以分配药师
     */
    public boolean canAssignPharmacist() {
        return this == SUBMITTED || this == ASSIGNED_PHARMACIST;
    }

    /**
     * 判断是否可以升级医生
     *
     * @return 是否可以升级医生
     */
    public boolean canEscalateToDoctor() {
        return this == PHARMACIST_REVIEW || this == ADVICE_READY;
    }

    /**
     * 判断是否可以驳回
     *
     * @return 是否可以驳回
     */
    public boolean canReject() {
        return !this.isCompleted();
    }
}

