package com.sqlapk.medicine.module.medication.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 用药计划项 DO
 *
 * @author sqlapk
 */
@TableName("med_plan_items")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MedPlanItemDO extends BaseDO {

    /**
     * 计划项ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 计划ID
     */
    private Long planId;
    
    /**
     * 药品名称
     */
    private String drugName;
    
    /**
     * 单次剂量
     */
    private String dosage;
    
    /**
     * 频次
     */
    private String frequency;
    
    /**
     * 时间点数组(JSON格式)
     */
    private String timePoints;
    
    /**
     * 餐前后关系(0-独立,1-餐前,2-餐后,3-餐中)
     */
    private Integer mealRelation;
    
    /**
     * 用法说明
     */
    private String instructions;
    
    /**
     * 排序
     */
    private Integer sortOrder;

}
