package com.sqlapk.medicine.module.medication.dal.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqlapk.medicine.module.medication.dal.dataobject.TodayTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 今日待办 Mapper
 *
 * @author sqlapk
 */
@Mapper
public interface TodayTaskMapper extends BaseMapper<TodayTaskDO> {

    /**
     * 根据患者ID和任务日期查询待办任务
     *
     * @param patientId 患者ID
     * @param taskDate 任务日期
     * @return 待办任务列表
     */
    List<TodayTaskDO> selectByPatientIdAndTaskDate(@Param("patientId") Long patientId, 
                                                   @Param("taskDate") LocalDate taskDate);

    /**
     * 根据计划项ID和任务日期查询待办任务
     *
     * @param planItemId 计划项ID
     * @param taskDate 任务日期
     * @return 待办任务列表
     */
    List<TodayTaskDO> selectByPlanItemIdAndTaskDate(@Param("planItemId") Long planItemId, 
                                                    @Param("taskDate") LocalDate taskDate);

    /**
     * 根据计划项ID、任务日期和时间查询待办任务
     *
     * @param planItemId 计划项ID
     * @param taskDate 任务日期
     * @param scheduledTime 计划时间
     * @return 待办任务
     */
    TodayTaskDO selectByPlanItemAndDateTime(@Param("planItemId") Long planItemId, 
                                           @Param("taskDate") LocalDate taskDate, 
                                           @Param("scheduledTime") LocalTime scheduledTime);

    /**
     * 根据计划ID删除待办任务
     *
     * @param planId 计划ID
     * @return 删除数量
     */
    int deleteByPlanId(@Param("planId") Long planId);

    /**
     * 根据计划项ID删除待办任务
     *
     * @param planItemId 计划项ID
     * @return 删除数量
     */
    int deleteByPlanItemId(@Param("planItemId") Long planItemId);

    /**
     * 根据状态查询待办任务
     *
     * @param status 状态
     * @return 待办任务列表
     */
    List<TodayTaskDO> selectByStatus(@Param("status") Integer status);

}
