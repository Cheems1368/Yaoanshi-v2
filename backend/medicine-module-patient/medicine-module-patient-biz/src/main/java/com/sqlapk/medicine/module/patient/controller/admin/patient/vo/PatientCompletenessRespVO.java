package com.sqlapk.medicine.module.patient.controller.admin.patient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 患者档案完整性响应 VO
 */
@Data
@Builder
@Schema(description = "管理后台 - 患者档案完整性响应")
public class PatientCompletenessRespVO {

    @Schema(description = "患者ID", example = "1")
    private Long patientId;

    @Schema(description = "完整性得分", example = "85.5")
    private Double completenessScore;

    @Schema(description = "完整性等级", example = "GOOD")
    private String completenessLevel;

    @Schema(description = "完整性描述", example = "档案基本完整，信息较详细")
    private String completenessDescription;

    @Schema(description = "必填字段是否完整", example = "true")
    private Boolean requiredFieldsComplete;

    @Schema(description = "重要字段是否完整", example = "false")
    private Boolean importantFieldsComplete;

    @Schema(description = "缺失的必填字段")
    private List<String> missingRequiredFields;

    @Schema(description = "缺失的重要字段")
    private List<String> missingImportantFields;

    @Schema(description = "缺失字段总数", example = "2")
    private Integer totalMissingFields;

    @Schema(description = "是否可以通过审核", example = "true")
    private Boolean auditPassable;

    @Schema(description = "检查时间")
    private LocalDateTime checkTime;

}
