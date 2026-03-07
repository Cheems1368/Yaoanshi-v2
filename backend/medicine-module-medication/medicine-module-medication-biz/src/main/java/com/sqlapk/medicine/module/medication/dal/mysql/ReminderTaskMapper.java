package com.sqlapk.medicine.module.medication.dal.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqlapk.medicine.module.medication.dal.dataobject.ReminderTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒任务 Mapper
 *
 * @author sqlapk
 */
@Mapper
public interface ReminderTaskMapper extends BaseMapper<ReminderTaskDO> {

    /**
     * 根据患者ID查询提醒任务
     *
     * @param patientId 患者ID
     * @return 提醒任务列表
     */
    List<ReminderTaskDO> selectByPatientId(@Param("patientId") Long patientId);

    /**
     * 根据状态查询提醒任务
     *
     * @param status 状态
     * @return 提醒任务列表
     */
    List<ReminderTaskDO> selectByStatus(@Param("status") Integer status);

    /**
     * 查询待执行的提醒任务
     *
     * @param currentTime 当前时间
     * @return 待执行任务列表
     */
    List<ReminderTaskDO> selectPendingTasks(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据今日待办任务ID查询提醒任务
     *
     * @param todayTaskId 今日待办任务ID
     * @return 提醒任务列表
     */
    List<ReminderTaskDO> selectByTodayTaskId(@Param("todayTaskId") Long todayTaskId);

    /**
     * 根据计划提醒时间范围查询任务
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 提醒任务列表
     */
    List<ReminderTaskDO> selectByScheduledTimeRange(@Param("startTime") LocalDateTime startTime, 
                                                    @Param("endTime") LocalDateTime endTime);

}
