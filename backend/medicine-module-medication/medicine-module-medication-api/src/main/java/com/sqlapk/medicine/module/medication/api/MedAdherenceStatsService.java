package com.sqlapk.medicine.module.medication.api;

import com.sqlapk.medicine.module.medication.api.dto.AdherenceStatsRespDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 依从统计服务 API
 *
 * @author sqlapk
 */
public interface MedAdherenceStatsService {

    /**
     * 计算并生成依从统计
     *
     * @param patientId 患者ID
     * @param period 统计周期(7-7天,30-30天,90-90天)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计记录ID
     */
    Long calculateAndGenerateStats(Long patientId, Integer period, LocalDate startDate, LocalDate endDate);

    /**
     * 获取患者最新依从统计
     *
     * @param patientId 患者ID
     * @param period 统计周期
     * @return 依从统计
     */
    AdherenceStatsRespDTO getLatestStats(Long patientId, Integer period);

    /**
     * 获取患者指定日期范围的依从统计
     *
     * @param patientId 患者ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 依从统计
     */
    AdherenceStatsRespDTO getStatsByDateRange(Long patientId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取患者历史依从统计
     *
     * @param patientId 患者ID
     * @param period 统计周期
     * @return 历史统计列表
     */
    List<AdherenceStatsRespDTO> getHistoryStats(Long patientId, Integer period);

    /**
     * 批量计算患者依从统计
     *
     * @param patientIds 患者ID列表
     * @param period 统计周期
     */
    void batchCalculateStats(List<Long> patientIds, Integer period);

    /**
     * 重新计算依从统计
     *
     * @param patientId 患者ID
     * @param period 统计周期
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    void recalculateStats(Long patientId, Integer period, LocalDate startDate, LocalDate endDate);

    /**
     * 获取患者7天依从统计
     *
     * @param patientId 患者ID
     * @return 7天依从统计
     */
    AdherenceStatsRespDTO get7DayStats(Long patientId);

    /**
     * 获取患者30天依从统计
     *
     * @param patientId 患者ID
     * @return 30天依从统计
     */
    AdherenceStatsRespDTO get30DayStats(Long patientId);

    /**
     * 获取患者90天依从统计
     *
     * @param patientId 患者ID
     * @return 90天依从统计
     */
    AdherenceStatsRespDTO get90DayStats(Long patientId);

    /**
     * 检查统计是否需要更新
     *
     * @param patientId 患者ID
     * @param period 统计周期
     * @return 是否需要更新
     */
    boolean needsUpdate(Long patientId, Integer period);

}
