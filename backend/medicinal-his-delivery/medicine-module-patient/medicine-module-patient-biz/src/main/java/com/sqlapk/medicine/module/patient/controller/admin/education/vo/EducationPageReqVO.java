package com.sqlapk.medicine.module.patient.controller.admin.education.vo;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用药教育分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EducationPageReqVO extends PageParam {

    @Schema(description = "药品名称", example = "王五")
    private String medicineName;

    @Schema(description = "患者ID", example = "2884")
    private Long patientId;

    @Schema(description = "疾病")
    private String disease;

    @Schema(description = "教育内容")
    private String content;

    @Schema(description = "教育依据")
    private String contentBasis;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
