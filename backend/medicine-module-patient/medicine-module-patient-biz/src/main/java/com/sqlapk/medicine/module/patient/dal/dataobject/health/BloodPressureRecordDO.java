package com.sqlapk.medicine.module.patient.dal.dataobject.health;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 血压记录 DO
 *
 * @author sqlapk
 */
@TableName("blood_pressure_records")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodPressureRecordDO extends BaseDO {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
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
     * 数据来源（手动录入/设备同步/医生录入）
     */
    private String dataSource;
    
    /**
     * 是否异常（0-正常，1-异常）
     */
    private Integer isAbnormal;
    
    /**
     * 异常类型（高血压/低血压/心率异常）
     */
    private String abnormalType;
    
    /**
     * 测量状态（0-正常，1-餐前，2-餐后，3-运动后）
     */
    private Integer measureStatus;
}
