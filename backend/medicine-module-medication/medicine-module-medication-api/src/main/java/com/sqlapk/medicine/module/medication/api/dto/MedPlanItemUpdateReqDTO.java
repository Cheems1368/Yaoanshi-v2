package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用药计划项更新请求 DTO
 *
 * @author sqlapk
 */
@Data
public class MedPlanItemUpdateReqDTO {

    /**
     * 计划项ID
     */
    private Long id;

    /**
     * 药品名称
     */
    @NotBlank(message = "药品名称不能为空")
    private String drugName;

    /**
     * 单次剂量
     */
    @NotBlank(message = "单次剂量不能为空")
    private String dosage;

    /**
     * 频次
     */
    @NotBlank(message = "频次不能为空")
    private String frequency;

    /**
     * 时间点列表
     */
    @NotNull(message = "时间点不能为空")
    private List<String> timePoints;

    /**
     * 餐前后关系(0-独立,1-餐前,2-餐后,3-餐中)
     */
    @NotNull(message = "餐前后关系不能为空")
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
