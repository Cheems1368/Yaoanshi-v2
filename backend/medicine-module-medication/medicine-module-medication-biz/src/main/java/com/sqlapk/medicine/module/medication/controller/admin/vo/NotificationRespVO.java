package com.sqlapk.medicine.module.medication.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用药通知响应VO
 */
@Schema(description = "用药通知响应")
@Data
public class NotificationRespVO {

    @Schema(description = "通知ID", example = "1")
    private Long id;

    @Schema(description = "患者ID", example = "1")
    private Long patientId;

    @Schema(description = "用药计划ID", example = "1")
    private Long medicationPlanId;

    @Schema(description = "通知类型", example = "MEDICATION_REMINDER")
    private String type;

    @Schema(description = "通知标题", example = "用药提醒")
    private String title;

    @Schema(description = "通知内容", example = "请按时服药")
    private String content;

    @Schema(description = "通知状态", example = "SENT")
    private String status;

    @Schema(description = "发送时间", example = "2024-01-01T08:00:00")
    private LocalDateTime sendTime;

    @Schema(description = "创建时间", example = "2024-01-01T08:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-01-01T08:00:00")
    private LocalDateTime updateTime;
}
