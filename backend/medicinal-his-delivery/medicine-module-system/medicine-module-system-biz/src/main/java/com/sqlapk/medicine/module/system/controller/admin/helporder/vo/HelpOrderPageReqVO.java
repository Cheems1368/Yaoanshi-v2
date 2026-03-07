// 修改文件: HelpOrderPageReqVO.java
package com.sqlapk.medicine.module.system.controller.admin.helporder.vo;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 工单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HelpOrderPageReqVO extends PageParam {

    @Schema(description = "工单编号", example = "1024")
    private Long id;

    @Schema(description = "患者编号", example = "123")
    private Long patientId;

    @Schema(description = "分配药师编号", example = "456")
    private Long assignedPharmacistId;

    @Schema(description = "分配医生编号", example = "789")
    private Long assignedDoctorId;

    @Schema(description = "工单标题", example = "用药咨询")
    private String title;

    @Schema(description = "工单状态", example = "SUBMITTED")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}