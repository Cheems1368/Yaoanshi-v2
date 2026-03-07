package com.sqlapk.medicine.module.medication.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * 用药计划 DO
 *
 * @author sqlapk
 */
@TableName("med_plans")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MedPlanDO extends BaseDO {

    /**
     * 计划ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 计划名称
     */
    private String planName;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 状态(1-活跃,2-暂停,3-终止)
     */
    private Integer status;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 计划描述
     */
    private String description;
    
    /**
     * 用药说明
     */
    private String instructions;

}
