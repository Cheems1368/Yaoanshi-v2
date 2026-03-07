package com.sqlapk.medicine.module.patient.controller.admin.questions.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.sqlapk.medicine.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 问卷问题 Excel 导出 Request VO，参数和 QuestionsPageReqVO 是一致的")
@Data
public class QuestionsExportReqVO {

    @Schema(description = "问卷内容")
    private String content;

    @Schema(description = "问卷ID", example = "18529")
    private Long surveyId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
