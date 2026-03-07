package com.sqlapk.medicine.module.system.api.helporder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 时间线事件 DTO")
@Data
public class TimelineEventDTO {

    @Schema(description = "事件时间", example = "2024-01-01T10:00:00Z")
    private String at;

    @Schema(description = "事件类型", example = "SUBMITTED")
    private String event;

    @Schema(description = "操作人ID", example = "123")
    private Long by;

    @Schema(description = "目标人ID", example = "456")
    private Long to;
}

