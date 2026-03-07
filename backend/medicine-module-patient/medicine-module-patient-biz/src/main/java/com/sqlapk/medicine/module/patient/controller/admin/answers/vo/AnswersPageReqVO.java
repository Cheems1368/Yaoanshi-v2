package com.sqlapk.medicine.module.patient.controller.admin.answers.vo;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 问卷分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AnswersPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "7786")
    private Long userId;

    @Schema(description = "问题ID", example = "18445")
    private Long questionId;

    @Schema(description = "选项ID", example = "18404")
    private Long optionId;

    @Schema(description = "答案值")
    private Integer value;

    private String content;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
