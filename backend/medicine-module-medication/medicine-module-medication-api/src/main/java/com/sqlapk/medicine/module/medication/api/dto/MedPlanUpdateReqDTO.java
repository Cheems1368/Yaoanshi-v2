package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 用药计划更新请求 DTO
 *
 * @author sqlapk
 */
@Data
public class MedPlanUpdateReqDTO {

    /**
     * 计划ID
     */
    @NotNull(message = "计划ID不能为空")
    private Long id;

    /**
     * 计划名称
     */
    @NotBlank(message = "计划名称不能为空")
    private String planName;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 计划描述
     */
    private String description;

    /**
     * 用药说明
     */
    private String instructions;

    /**
     * 计划项列表
     */
    @NotNull(message = "计划项不能为空")
    private List<MedPlanItemUpdateReqDTO> items;

}
