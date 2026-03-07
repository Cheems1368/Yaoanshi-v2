package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 今日待办响应 DTO
 *
 * @author sqlapk
 */
@Data
public class TodayTaskRespDTO {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 计划项ID
     */
    private Long planItemId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 任务日期
     */
    private LocalDate taskDate;

    /**
     * 计划时间
     */
    private LocalTime scheduledTime;

    /**
     * 任务状态(0-待完成,1-已完成,2-已跳过)
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;

    /**
     * 备注
     */
    private String notes;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 单次剂量
     */
    private String dosage;

    /**
     * 用法说明
     */
    private String instructions;

    /**
     * 餐前后关系名称
     */
    private String mealRelationName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
