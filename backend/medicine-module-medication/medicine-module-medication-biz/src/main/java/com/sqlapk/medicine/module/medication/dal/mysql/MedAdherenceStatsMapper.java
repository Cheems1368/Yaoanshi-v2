package com.sqlapk.medicine.module.medication.dal.mysql;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedAdherenceStatsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 依从统计 Mapper
 *
 * @author sqlapk
 */
@Mapper
public interface MedAdherenceStatsMapper extends BaseMapper<MedAdherenceStatsDO> {

    /**
     * 根据患者ID和周期获取最新统计
     */
    @Select("SELECT * FROM med_adherence_stats WHERE patient_id = #{patientId} " +
            "AND period = #{period} AND deleted = 0 ORDER BY statistics_date DESC LIMIT 1")
    MedAdherenceStatsDO selectLatestByPatientIdAndPeriod(@Param("patientId") Long patientId,
                                                          @Param("period") Integer period);

    /**
     * 根据患者ID和日期范围获取统计
     */
    @Select("SELECT * FROM med_adherence_stats WHERE patient_id = #{patientId} " +
            "AND start_date = #{startDate} AND end_date = #{endDate} AND deleted = 0")
    MedAdherenceStatsDO selectByPatientIdAndDateRange(@Param("patientId") Long patientId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    /**
     * 根据患者ID获取所有统计记录
     */
    @Select("SELECT * FROM med_adherence_stats WHERE patient_id = #{patientId} " +
            "AND deleted = 0 ORDER BY statistics_date DESC")
    List<MedAdherenceStatsDO> selectByPatientId(@Param("patientId") Long patientId);

    /**
     * 根据患者ID和周期获取历史统计
     */
    @Select("SELECT * FROM med_adherence_stats WHERE patient_id = #{patientId} " +
            "AND period = #{period} AND deleted = 0 ORDER BY statistics_date DESC")
    List<MedAdherenceStatsDO> selectHistoryByPatientIdAndPeriod(@Param("patientId") Long patientId,
                                                                @Param("period") Integer period);

    /**
     * 统计指定日期范围内的任务数
     */
    @Select("SELECT COUNT(*) FROM med_today_tasks WHERE patient_id = #{patientId} " +
            "AND task_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    Integer countTotalTasks(@Param("patientId") Long patientId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);

    /**
     * 统计指定日期范围内的完成任务数
     */
    @Select("SELECT COUNT(*) FROM med_today_tasks WHERE patient_id = #{patientId} " +
            "AND task_date BETWEEN #{startDate} AND #{endDate} " +
            "AND status = 1 AND deleted = 0")
    Integer countCompletedTasks(@Param("patientId") Long patientId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    /**
     * 统计指定日期范围内的跳过任务数
     */
    @Select("SELECT COUNT(*) FROM med_today_tasks WHERE patient_id = #{patientId} " +
            "AND task_date BETWEEN #{startDate} AND #{endDate} " +
            "AND status = 2 AND deleted = 0")
    Integer countSkippedTasks(@Param("patientId") Long patientId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

    /**
     * 获取患者指定日期范围内的常用药品
     */
    @Select("SELECT i.drug_name, COUNT(*) as count FROM med_today_tasks t " +
            "INNER JOIN med_plan_items i ON t.plan_item_id = i.id " +
            "WHERE t.patient_id = #{patientId} AND t.task_date BETWEEN #{startDate} AND #{endDate} " +
            "AND t.status = 1 AND t.deleted = 0 " +
            "GROUP BY i.drug_name ORDER BY count DESC LIMIT 5")
    List<DrugStatsDTO> selectTopMedications(@Param("patientId") Long patientId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * 药品统计DTO
     */
    class DrugStatsDTO {
        private String drugName;
        private Integer count;

        public String getDrugName() { return drugName; }
        public void setDrugName(String drugName) { this.drugName = drugName; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    /**
     * 根据条件查询统计记录
     */
    default List<MedAdherenceStatsDO> selectByCondition(Long patientId, Integer period, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<MedAdherenceStatsDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, MedAdherenceStatsDO::getPatientId, patientId)
               .eq(period != null, MedAdherenceStatsDO::getPeriod, period)
               .ge(startDate != null, MedAdherenceStatsDO::getStartDate, startDate)
               .le(endDate != null, MedAdherenceStatsDO::getEndDate, endDate)
               .orderByDesc(MedAdherenceStatsDO::getStatisticsDate);
        return selectList(wrapper);
    }

}
