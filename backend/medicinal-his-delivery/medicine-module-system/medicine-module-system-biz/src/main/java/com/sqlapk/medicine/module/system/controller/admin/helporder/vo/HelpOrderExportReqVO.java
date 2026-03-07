package com.sqlapk.medicine.module.system.controller.admin.helporder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 工单导出 Request VO")
@Data
public class HelpOrderExportReqVO {

    @Schema(description = "工单状态", example = "PHARMACIST_REVIEW")
    private String status;

    @Schema(description = "患者编号", example = "123")
    private Long patientId;

    @Schema(description = "分配药师编号", example = "456")
    private Long assignedPharmacistId;

    @Schema(description = "分配医生编号", example = "789")
    private Long assignedDoctorId;

    @Schema(description = "工单标题", example = "用药咨询")
    private String title;

    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;
}

