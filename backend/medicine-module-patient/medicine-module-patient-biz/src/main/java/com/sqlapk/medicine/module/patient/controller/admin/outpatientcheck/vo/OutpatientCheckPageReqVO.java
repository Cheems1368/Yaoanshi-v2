package com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 处方审核分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OutpatientCheckPageReqVO extends PageParam {

    @Schema(description = "门诊药学ID", example = "1346")
    private Long outpatientId;

    @Schema(description = "不合理用药名称", example = "李四")
    private String medicineName;

    @Schema(description = "处方医生", example = "26050")
    private Long doctorId;

    @Schema(description = "处方ID", example = "27266")
    private Long prescriptionId;

    @Schema(description = "问题描述")
    private String issueDesc;

    @Schema(description = "问题代码")
    private String issueCode;

    @Schema(description = "干预结果")
    private String interveneResult;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
