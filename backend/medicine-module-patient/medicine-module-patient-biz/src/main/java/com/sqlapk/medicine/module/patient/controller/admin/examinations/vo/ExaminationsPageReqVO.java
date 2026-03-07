package com.sqlapk.medicine.module.patient.controller.admin.examinations.vo;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 检查项目分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExaminationsPageReqVO extends PageParam {

    @Schema(description = "检查项目名称", example = "芋艿")
    private String name;

    @Schema(description = "价格", example = "17583")
    private BigDecimal price;

    @Schema(description = "描述", example = "随便")
    private String description;

    @Schema(description = "创建日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
