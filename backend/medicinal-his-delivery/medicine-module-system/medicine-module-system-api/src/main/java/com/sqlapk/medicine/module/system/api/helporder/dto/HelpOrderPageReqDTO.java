package com.sqlapk.medicine.module.system.api.helporder.dto;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 工单分页 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HelpOrderPageReqDTO extends PageParam {

    @Schema(description = "工单状态", example = "PHARMACIST_REVIEW")
    private HelpOrderStatus status;

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

