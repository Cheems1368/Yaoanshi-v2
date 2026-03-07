package com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 处方检查项目关系分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PrescriptionExaminationsPageReqVO extends PageParam {

    @Schema(description = "处方ID", example = "7737")
    private Long prescriptionId;

    @Schema(description = "检查项目ID", example = "12994")
    private Long examinationId;

    @Schema(description = "检查结果")
    private String result;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
