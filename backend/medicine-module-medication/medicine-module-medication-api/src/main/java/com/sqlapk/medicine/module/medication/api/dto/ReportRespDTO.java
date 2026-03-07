package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报告响应 DTO
 *
 * @author sqlapk
 */
@Data
public class ReportRespDTO {

    /**
     * 报告ID
     */
    private Long id;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 报告类型(1-周报告,2-月报告,3-季度报告,4-年度报告)
     */
    private Integer reportType;

    /**
     * 报告类型名称
     */
    private String reportTypeName;

    /**
     * 报告标题
     */
    private String title;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 报告内容
     */
    private ReportContentDTO content;

    /**
     * 依从性摘要
     */
    private AdherenceSummaryDTO adherenceSummary;

    /**
     * 用药摘要
     */
    private MedicationSummaryDTO medicationSummary;

    /**
     * 健康指标
     */
    private HealthIndicatorsDTO healthIndicators;

    /**
     * 生成时间
     */
    private LocalDateTime generationTime;

    /**
     * 报告状态(1-生成中,2-已完成,3-生成失败)
     */
    private Integer status;

    /**
     * 报告状态名称
     */
    private String statusName;

    /**
     * 口径版本号
     */
    private Integer version;

    /**
     * 报告文件路径
     */
    private String filePath;

    /**
     * 报告文件大小(字节)
     */
    private Long fileSize;

    /**
     * 报告文件格式(PDF,HTML,JSON)
     */
    private String fileFormat;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 报告内容 DTO
     */
    @Data
    public static class ReportContentDTO {
        /**
         * 摘要
         */
        private String summary;

        /**
         * 建议
         */
        private String recommendations;

        /**
         * 注意事项
         */
        private String notes;
    }

    /**
     * 依从性摘要 DTO
     */
    @Data
    public static class AdherenceSummaryDTO {
        /**
         * 依从率
         */
        private java.math.BigDecimal adherenceRate;

        /**
         * 总任务数
         */
        private Integer totalTasks;

        /**
         * 完成任务数
         */
        private Integer completedTasks;

        /**
         * 遗漏任务数
         */
        private Integer missedTasks;
    }

    /**
     * 用药摘要 DTO
     */
    @Data
    public static class MedicationSummaryDTO {
        /**
         * 总药品数
         */
        private Integer totalMedications;

        /**
         * 主要药品
         */
        private String topMedication;

        /**
         * 平均完成率
         */
        private java.math.BigDecimal averageCompletionRate;
    }

    /**
     * 健康指标 DTO
     */
    @Data
    public static class HealthIndicatorsDTO {
        /**
         * 血压
         */
        private String bloodPressure;

        /**
         * 心率
         */
        private String heartRate;

        /**
         * 体重
         */
        private String weight;

        /**
         * 血糖
         */
        private String bloodSugar;

        /**
         * BMI
         */
        private String bmi;
    }

}
