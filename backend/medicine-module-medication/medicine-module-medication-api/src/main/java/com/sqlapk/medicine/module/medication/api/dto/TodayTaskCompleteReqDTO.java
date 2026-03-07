package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 今日待办完成请求 DTO
 *
 * @author sqlapk
 */
@Data
public class TodayTaskCompleteReqDTO {

    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long id;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;

    /**
     * 备注
     */
    private String notes;

}
