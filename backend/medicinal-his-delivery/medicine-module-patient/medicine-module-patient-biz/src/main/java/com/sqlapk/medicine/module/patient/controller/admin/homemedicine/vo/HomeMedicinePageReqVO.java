package com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo;

import com.sqlapk.medicine.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用药清单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HomeMedicinePageReqVO extends PageParam {

    @Schema(description = "居家药学ID", example = "20116")
    private Long homeId;

    @Schema(description = "药品名称", example = "张三")
    private String medicineName;

    @Schema(description = "医师ID", example = "23697")
    private Long doctorId;

    @Schema(description = "处方医师", example = "赵六")
    private String doctorName;

    @Schema(description = "剂量")
    private String dosage;

    @Schema(description = "适应症")
    private String indications;

    @Schema(description = "用法")
    private String medicineUsage;

    @Schema(description = "开始日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "访视时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "特殊说明")
    private String comment;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
