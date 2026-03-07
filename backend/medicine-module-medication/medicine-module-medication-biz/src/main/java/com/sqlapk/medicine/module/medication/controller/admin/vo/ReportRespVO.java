package com.sqlapk.medicine.module.medication.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用药报告响应VO
 */
@Schema(description = "用药报告响应")
@Data
public class ReportRespVO {

    @Schema(description = "报告ID", example = "1")
    private Long id;

    @Schema(description = "患者ID", example = "1")
    private Long patientId;

    @Schema(description = "报告类型", example = "ADHERENCE_REPORT")
    private String type;

    @Schema(description = "报告标题", example = "用药依从性报告")
    private String title;

    @Schema(description = "报告内容", example = "患者用药依从性良好")
    private String content;

    @Schema(description = "报告状态", example = "GENERATED")
    private String status;

    @Schema(description = "生成时间", example = "2024-01-01T08:00:00")
    private LocalDateTime generateTime;

    @Schema(description = "文件路径", example = "/reports/patient_1_report_20240101.pdf")
    private String filePath;

    @Schema(description = "创建时间", example = "2024-01-01T08:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-01-01T08:00:00")
    private LocalDateTime updateTime;
}
