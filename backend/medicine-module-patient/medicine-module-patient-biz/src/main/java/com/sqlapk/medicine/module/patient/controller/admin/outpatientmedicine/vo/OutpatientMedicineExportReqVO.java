package com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sqlapk.medicine.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 药学门诊 Excel 导出 Request VO，参数和 OutpatientMedicinePageReqVO 是一致的")
@Data
public class OutpatientMedicineExportReqVO {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "门诊药学ID", example = "20362")
    private Long outpatientId;

    @Schema(description = "药学门诊ID", example = "21630")
    private Long meicineId;

}
