package com.sqlapk.medicine.module.medication.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用药打卡响应VO
 */
@Schema(description = "用药打卡响应")
@Data
public class CheckinRespVO {

    @Schema(description = "打卡ID", example = "1")
    private Long id;

    @Schema(description = "患者ID", example = "1")
    private Long patientId;

    @Schema(description = "用药计划ID", example = "1")
    private Long medicationPlanId;

    @Schema(description = "打卡时间", example = "2024-01-01T08:00:00")
    private LocalDateTime checkinTime;

    @Schema(description = "打卡状态", example = "COMPLETED")
    private String status;

    @Schema(description = "备注", example = "按时服药")
    private String remarks;

    @Schema(description = "创建时间", example = "2024-01-01T08:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-01-01T08:00:00")
    private LocalDateTime updateTime;
}
