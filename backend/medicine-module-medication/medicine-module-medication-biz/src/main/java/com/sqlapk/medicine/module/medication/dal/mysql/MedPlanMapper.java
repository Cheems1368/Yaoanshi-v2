package com.sqlapk.medicine.module.medication.dal.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedPlanDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 用药计划 Mapper
 *
 * @author sqlapk
 */
@Mapper
public interface MedPlanMapper extends BaseMapper<MedPlanDO> {

    /**
     * 根据患者ID查询用药计划
     *
     * @param patientId 患者ID
     * @return 用药计划列表
     */
    List<MedPlanDO> selectByPatientId(@Param("patientId") Long patientId);

    /**
     * 根据状态查询用药计划
     *
     * @param status 状态
     * @return 用药计划列表
     */
    List<MedPlanDO> selectByStatus(@Param("status") Integer status);

    /**
     * 根据患者ID和状态查询用药计划
     *
     * @param patientId 患者ID
     * @param status 状态
     * @return 用药计划列表
     */
    List<MedPlanDO> selectByPatientIdAndStatus(@Param("patientId") Long patientId, 
                                               @Param("status") Integer status);

    /**
     * 根据状态和日期范围查询用药计划
     *
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用药计划列表
     */
    @Select("SELECT * FROM med_plans WHERE status = #{status} " +
            "AND start_date <= #{endDate} " +
            "AND (end_date IS NULL OR end_date >= #{startDate}) " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<MedPlanDO> selectByStatusAndDateRange(@Param("status") Integer status,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

}
