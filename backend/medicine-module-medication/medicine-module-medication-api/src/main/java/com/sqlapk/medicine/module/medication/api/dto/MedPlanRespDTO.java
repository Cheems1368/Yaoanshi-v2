package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用药计划响应 DTO
 *
 * @author sqlapk
 */
@Data
public class MedPlanRespDTO {

    /**
     * 计划ID
     */
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
     * 状态名称
     */
    private String statusName;

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

    /**
     * 计划项列表
     */
    private List<MedPlanItemRespDTO> items;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
