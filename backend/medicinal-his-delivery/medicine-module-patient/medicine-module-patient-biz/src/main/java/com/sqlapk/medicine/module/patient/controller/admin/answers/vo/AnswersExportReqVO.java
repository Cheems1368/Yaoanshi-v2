package com.sqlapk.medicine.module.patient.controller.admin.answers.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.sqlapk.medicine.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 问卷 Excel 导出 Request VO，参数和 AnswersPageReqVO 是一致的")
@Data
public class AnswersExportReqVO {

    @Schema(description = "用户ID", example = "7786")
    private Long userId;

    @Schema(description = "问题ID", example = "18445")
    private Long questionId;

    @Schema(description = "选项ID", example = "18404")
    private Long optionId;

    @Schema(description = "答案值")
    private Integer value;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
