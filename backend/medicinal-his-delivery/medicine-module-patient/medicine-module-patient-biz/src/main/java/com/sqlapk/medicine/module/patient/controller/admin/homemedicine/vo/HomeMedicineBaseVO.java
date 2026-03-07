package com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static com.sqlapk.medicine.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

/**
 * 用药清单 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class HomeMedicineBaseVO {

    @Schema(description = "居家药学ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20116")
    @NotNull(message = "居家药学ID不能为空")
    private Long homeId;

    @Schema(description = "药品名称", example = "张三")
    private String medicineName;

    @Schema(description = "医师ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23697")
    @NotNull(message = "医师ID不能为空")
    private Long doctorId;

    @Schema(description = "处方医师", example = "赵六")
    private String doctorName;

    @Schema(description = "剂量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "剂量不能为空")
    private String dosage;

    @Schema(description = "适应症", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "适应症不能为空")
    private String indications;

    @Schema(description = "用法", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用法不能为空")
    private String medicineUsage;

    @Schema(description = "开始日期")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime startTime;

    @Schema(description = "访视时间")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime endTime;

    @Schema(description = "特殊说明")
    private String comment;

}
