package com.sqlapk.medicine.module.patient.controller.admin.options.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.sqlapk.medicine.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 问题选项分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OptionsPageReqVO extends PageParam {

    @Schema(description = "选项内容")
    private String content;

    @Schema(description = "选项值")
    private Integer value;

    @Schema(description = "选项分值")
    private Integer score;

    @Schema(description = "问题ID", example = "8349")
    private Long questionId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
