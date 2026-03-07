package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 依从统计响应 DTO
 *
 * @author sqlapk
 */
@Data
public class AdherenceStatsRespDTO {

    /**
     * 统计ID
     */
    private Long id;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 统计周期(7-7天,30-30天,90-90天)
     */
    private Integer period;

    /**
     * 统计周期名称
     */
    private String periodName;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 总任务数
     */
    private Integer totalTasks;

    /**
     * 完成任务数
     */
    private Integer completedTasks;

    /**
     * 跳过任务数
     */
    private Integer missedTasks;

    /**
     * 延迟任务数
     */
    private Integer delayedTasks;

    /**
     * 依从率
     */
    private BigDecimal adherenceRate;

    /**
     * 依从率百分比
     */
    private String adherenceRatePercent;

    /**
     * 常用药品Top
     */
    private List<DrugStatsDTO> topMedications;

    /**
     * 统计日期
     */
    private LocalDate statisticsDate;

    /**
     * 统计版本号
     */
    private Integer version;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;

    /**
     * 更新时间
     */
    private java.time.LocalDateTime updateTime;

    /**
     * 药品统计DTO
     */
    @Data
    public static class DrugStatsDTO {
        /**
         * 药品名称
         */
        private String drugName;

        /**
         * 完成次数
         */
        private Integer count;

        /**
         * 完成率
         */
        private BigDecimal completionRate;

        /**
         * 完成率百分比
         */
        private String completionRatePercent;
    }

}
