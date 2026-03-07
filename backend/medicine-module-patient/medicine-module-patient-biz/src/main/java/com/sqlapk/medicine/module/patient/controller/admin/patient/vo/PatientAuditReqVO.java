package com.sqlapk.medicine.module.patient.controller.admin.patient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 患者档案审核请求 VO
 */
@Schema(description = "管理后台 - 患者档案审核请求")
public class PatientAuditReqVO {

    @Data
    @Schema(description = "提交审核请求")
    public static class SubmitReqVO {

        @Schema(description = "患者ID", required = true, example = "1")
        @NotNull(message = "患者ID不能为空")
        private Long patientId;

        @Schema(description = "审核人ID", required = true, example = "1")
        @NotNull(message = "审核人ID不能为空")
        private Long auditorId;

    }

    @Data
    @Schema(description = "审核请求")
    public static class AuditReqVO {

        @Schema(description = "患者ID", required = true, example = "1")
        @NotNull(message = "患者ID不能为空")
        private Long patientId;

        @Schema(description = "审核人ID", required = true, example = "1")
        @NotNull(message = "审核人ID不能为空")
        private Long auditorId;

        @Schema(description = "审核状态", required = true, example = "1")
        @NotNull(message = "审核状态不能为空")
        private Integer auditStatus;

        @Schema(description = "审核意见", example = "档案完整，审核通过")
        private String auditComment;

    }

    @Data
    @Schema(description = "批量审核请求")
    public static class BatchAuditReqVO {

        @Schema(description = "患者ID列表", required = true)
        @NotNull(message = "患者ID列表不能为空")
        private List<Long> patientIds;

        @Schema(description = "审核人ID", required = true, example = "1")
        @NotNull(message = "审核人ID不能为空")
        private Long auditorId;

        @Schema(description = "审核状态", required = true, example = "1")
        @NotNull(message = "审核状态不能为空")
        private Integer auditStatus;

        @Schema(description = "审核意见", example = "批量审核通过")
        private String auditComment;

    }

}
