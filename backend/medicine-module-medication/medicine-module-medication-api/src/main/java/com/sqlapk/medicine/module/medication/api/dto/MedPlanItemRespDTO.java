package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用药计划项响应 DTO
 *
 * @author sqlapk
 */
@Data
public class MedPlanItemRespDTO {

    /**
     * 计划项ID
     */
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
     * 时间点列表
     */
    private List<String> timePoints;

    /**
     * 餐前后关系(0-独立,1-餐前,2-餐后,3-餐中)
     */
    private Integer mealRelation;

    /**
     * 餐前后关系名称
     */
    private String mealRelationName;

    /**
     * 用法说明
     */
    private String instructions;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
