package com.sqlapk.medicine.module.patient.api.dto.health;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 血压记录响应 DTO
 *
 * @author sqlapk
 */
@Data
public class BloodPressureRecordRespDTO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 收缩压（mmHg）
     */
    private Integer systolicPressure;

    /**
     * 舒张压（mmHg）
     */
    private Integer diastolicPressure;

    /**
     * 心率（次/分钟）
     */
    private Integer heartRate;

    /**
     * 测量时间
     */
    private LocalDateTime measureTime;

    /**
     * 测量设备
     */
    private String deviceType;

    /**
     * 备注
     */
    private String notes;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 是否异常（0-正常，1-异常）
     */
    private Integer isAbnormal;

    /**
     * 异常类型
     */
    private String abnormalType;

    /**
     * 测量状态
     */
    private Integer measureStatus;

    /**
     * 血压等级（正常/高血压1级/高血压2级/高血压3级）
     */
    private String pressureLevel;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
