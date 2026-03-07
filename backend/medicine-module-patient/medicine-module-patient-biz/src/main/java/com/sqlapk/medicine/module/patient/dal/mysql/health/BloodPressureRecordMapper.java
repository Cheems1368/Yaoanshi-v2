package com.sqlapk.medicine.module.patient.dal.mysql.health;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqlapk.medicine.module.patient.dal.dataobject.health.BloodPressureRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 血压记录 Mapper
 *
 * @author sqlapk
 */
@Mapper
public interface BloodPressureRecordMapper extends BaseMapper<BloodPressureRecordDO> {

    /**
     * 获取患者最近的血压记录
     */
    @Select("SELECT * FROM blood_pressure_records WHERE patient_id = #{patientId} " +
            "AND deleted = 0 ORDER BY measure_time DESC LIMIT #{limit}")
    List<BloodPressureRecordDO> selectRecentRecords(@Param("patientId") Long patientId, 
                                                     @Param("limit") Integer limit);

    /**
     * 获取患者指定时间范围内的血压记录
     */
    @Select("SELECT * FROM blood_pressure_records WHERE patient_id = #{patientId} " +
            "AND measure_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY measure_time DESC")
    List<BloodPressureRecordDO> selectRecordsByTimeRange(@Param("patientId") Long patientId,
                                                         @Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 获取患者血压统计信息
     */
    @Select("SELECT " +
            "AVG(systolic_pressure) as avgSystolic, " +
            "AVG(diastolic_pressure) as avgDiastolic, " +
            "AVG(heart_rate) as avgHeartRate, " +
            "COUNT(*) as recordCount " +
            "FROM blood_pressure_records " +
            "WHERE patient_id = #{patientId} " +
            "AND measure_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0")
    BloodPressureStats selectBloodPressureStats(@Param("patientId") Long patientId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 血压统计结果
     */
    class BloodPressureStats {
        private Double avgSystolic;
        private Double avgDiastolic;
        private Double avgHeartRate;
        private Long recordCount;

        // Getters and Setters
        public Double getAvgSystolic() { return avgSystolic; }
        public void setAvgSystolic(Double avgSystolic) { this.avgSystolic = avgSystolic; }
        public Double getAvgDiastolic() { return avgDiastolic; }
        public void setAvgDiastolic(Double avgDiastolic) { this.avgDiastolic = avgDiastolic; }
        public Double getAvgHeartRate() { return avgHeartRate; }
        public void setAvgHeartRate(Double avgHeartRate) { this.avgHeartRate = avgHeartRate; }
        public Long getRecordCount() { return recordCount; }
        public void setRecordCount(Long recordCount) { this.recordCount = recordCount; }
    }
}
