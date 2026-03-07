package com.sqlapk.medicine.module.system.api.helporder.dto;

import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 工单 Response DTO")
@Data
public class HelpOrderRespDTO {

    @Schema(description = "工单编号", example = "1")
    private Long id;

    @Schema(description = "患者编号", example = "123")
    private Long patientId;

    @Schema(description = "分配药师编号", example = "456")
    private Long assignedPharmacistId;

    @Schema(description = "分配医生编号", example = "789")
    private Long assignedDoctorId;

    @Schema(description = "工单标题", example = "用药咨询")
    private String title;

    @Schema(description = "工单描述", example = "我想咨询一下这个药的用法")
    private String description;

    @Schema(description = "意向药品", example = "[\"阿司匹林\", \"布洛芬\"]")
    private List<String> intendedDrugs;

    @Schema(description = "附件列表", example = "[\"file1.jpg\", \"file2.pdf\"]")
    private List<String> attachments;

    @Schema(description = "是否需要医生", example = "false")
    private Boolean toDoctor;

    @Schema(description = "工单状态", example = "PHARMACIST_REVIEW")
    private HelpOrderStatus status;

    @Schema(description = "分配时间")
    private LocalDateTime assignedAt;

    @Schema(description = "升级时间")
    private LocalDateTime escalatedAt;

    @Schema(description = "处方就绪时间")
    private LocalDateTime prescriptionReadyAt;

    @Schema(description = "完成时间")
    private LocalDateTime doneAt;

    @Schema(description = "驳回时间")
    private LocalDateTime rejectedAt;

    @Schema(description = "驳回人")
    private Long rejectedBy;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "时间线")
    private List<TimelineEventDTO> timeline;
}

