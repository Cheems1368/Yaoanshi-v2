package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 报告生成请求 DTO
 *
 * @author sqlapk
 */
@Data
public class ReportGenerateReqDTO {

    /**
     * 患者ID
     */
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    /**
     * 报告类型(1-周报告,2-月报告,3-季度报告,4-年度报告)
     */
    @NotNull(message = "报告类型不能为空")
    private Integer reportType;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 报告标题
     */
    private String title;

    /**
     * 是否包含档案摘要
     */
    private Boolean includeProfileSummary = true;

    /**
     * 是否包含健康指标
     */
    private Boolean includeHealthIndicators = true;

    /**
     * 是否包含用药建议
     */
    private Boolean includeMedicationAdvice = true;

}
