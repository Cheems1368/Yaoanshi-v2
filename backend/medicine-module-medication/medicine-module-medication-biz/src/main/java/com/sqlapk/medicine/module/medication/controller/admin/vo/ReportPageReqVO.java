package com.sqlapk.medicine.module.medication.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 用药报告分页查询请求VO
 */
@Schema(description = "用药报告分页查询请求")
@Data
public class ReportPageReqVO {

    @Schema(description = "患者ID", example = "1")
    private Long patientId;

    @Schema(description = "报告类型", example = "ADHERENCE_REPORT")
    private String reportType;

    @Schema(description = "报告状态", example = "GENERATED")
    private String status;

    @Schema(description = "开始时间", example = "2024-01-01T00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2024-01-31T23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "页码", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
