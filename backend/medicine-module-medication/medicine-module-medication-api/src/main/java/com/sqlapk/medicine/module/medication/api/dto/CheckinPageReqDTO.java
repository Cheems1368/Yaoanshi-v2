package com.sqlapk.medicine.module.medication.api.dto;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 打卡记录分页查询请求 DTO
 *
 * @author sqlapk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CheckinPageReqDTO extends PageParam {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 用药计划ID
     */
    private Long planId;

    /**
     * 今日待办任务ID
     */
    private Long todayTaskId;

    /**
     * 打卡状态(0-待打卡,1-已打卡,2-已跳过,3-已撤销)
     */
    private Integer status;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 药品名称（模糊查询）
     */
    private String drugName;

}
