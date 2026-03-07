package com.sqlapk.medicine.module.medication.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 依从统计 DO
 *
 * @author sqlapk
 */
@TableName("med_adherence_stats")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MedAdherenceStatsDO extends BaseDO {

    /**
     * 统计ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 统计周期(7-7天,30-30天,90-90天)
     */
    private Integer period;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 总任务数
     */
    private Integer totalTasks;
    
    /**
     * 完成任务数
     */
    private Integer completedTasks;
    
    /**
     * 跳过任务数
     */
    private Integer missedTasks;
    
    /**
     * 延迟任务数
     */
    private Integer delayedTasks;
    
    /**
     * 依从率
     */
    private BigDecimal adherenceRate;
    
    /**
     * 常用药品Top（JSON格式）
     */
    private String topMedications;
    
    /**
     * 统计日期
     */
    private LocalDate statisticsDate;
    
    /**
     * 统计版本号
     */
    private Integer version;

}
