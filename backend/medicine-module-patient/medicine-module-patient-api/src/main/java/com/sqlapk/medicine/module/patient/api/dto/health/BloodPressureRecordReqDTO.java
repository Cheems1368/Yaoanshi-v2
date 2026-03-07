package com.sqlapk.medicine.module.patient.api.dto.health;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import java.time.LocalDateTime;

/**
 * 血压记录请求 DTO
 *
 * @author sqlapk
 */
@Data
public class BloodPressureRecordReqDTO {

    /**
     * 患者ID
     */
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    /**
     * 收缩压（mmHg）
     */
    @NotNull(message = "收缩压不能为空")
    @Min(value = 60, message = "收缩压不能低于60mmHg")
    @Max(value = 250, message = "收缩压不能高于250mmHg")
    private Integer systolicPressure;

    /**
     * 舒张压（mmHg）
     */
    @NotNull(message = "舒张压不能为空")
    @Min(value = 40, message = "舒张压不能低于40mmHg")
    @Max(value = 150, message = "舒张压不能高于150mmHg")
    private Integer diastolicPressure;

    /**
     * 心率（次/分钟）
     */
    @Min(value = 30, message = "心率不能低于30次/分钟")
    @Max(value = 200, message = "心率不能高于200次/分钟")
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
     * 数据来源（手动录入/设备同步/医生录入）
     */
    private String dataSource;

    /**
     * 测量状态（0-正常，1-餐前，2-餐后，3-运动后）
     */
    private Integer measureStatus;
}
