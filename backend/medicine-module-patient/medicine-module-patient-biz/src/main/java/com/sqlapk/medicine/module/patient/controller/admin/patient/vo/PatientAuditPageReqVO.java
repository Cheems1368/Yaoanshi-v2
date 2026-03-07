package com.sqlapk.medicine.module.patient.controller.admin.patient.vo;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 患者档案审核分页请求 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "管理后台 - 患者档案审核分页请求")
public class PatientAuditPageReqVO extends PageParam {

    @Schema(description = "患者ID", example = "1")
    private Long patientId;

    @Schema(description = "审核人ID", example = "1")
    private Long auditorId;

    @Schema(description = "审核状态", example = "1")
    private Integer auditStatus;

    @Schema(description = "完整性等级", example = "GOOD")
    private String completenessLevel;

    @Schema(description = "提交时间开始")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime submitTimeStart;

    @Schema(description = "提交时间结束")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime submitTimeEnd;

    @Schema(description = "审核时间开始")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime auditTimeStart;

    @Schema(description = "审核时间结束")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime auditTimeEnd;

}
