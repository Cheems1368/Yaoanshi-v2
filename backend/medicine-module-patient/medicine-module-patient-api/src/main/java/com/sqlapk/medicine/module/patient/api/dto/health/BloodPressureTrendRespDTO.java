package com.sqlapk.medicine.module.patient.api.dto.health;

import lombok.Data;

import java.util.List;

/**
 * 血压趋势响应 DTO
 *
 * @author sqlapk
 */
@Data
public class BloodPressureTrendRespDTO {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 统计周期（天数）
     */
    private Integer periodDays;

    /**
     * 平均收缩压
     */
    private Double avgSystolicPressure;

    /**
     * 平均舒张压
     */
    private Double avgDiastolicPressure;

    /**
     * 平均心率
     */
    private Double avgHeartRate;

    /**
     * 记录总数
     */
    private Long recordCount;

    /**
     * 最高收缩压
     */
    private Integer maxSystolicPressure;

    /**
     * 最低收缩压
     */
    private Integer minSystolicPressure;

    /**
     * 最高舒张压
     */
    private Integer maxDiastolicPressure;

    /**
     * 最低舒张压
     */
    private Integer minDiastolicPressure;

    /**
     * 异常记录数
     */
    private Long abnormalCount;

    /**
     * 趋势数据点
     */
    private List<TrendDataPoint> trendData;

    /**
     * 趋势数据点
     */
    @Data
    public static class TrendDataPoint {
        /**
         * 日期
         */
        private String date;

        /**
         * 收缩压
         */
        private Integer systolicPressure;

        /**
         * 舒张压
         */
        private Integer diastolicPressure;

        /**
         * 心率
         */
        private Integer heartRate;

        /**
         * 是否异常
         */
        private Boolean isAbnormal;
    }
}
