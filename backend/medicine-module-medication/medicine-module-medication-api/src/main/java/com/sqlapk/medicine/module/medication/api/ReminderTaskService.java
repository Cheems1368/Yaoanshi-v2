package com.sqlapk.medicine.module.medication.api;

import com.sqlapk.medicine.module.medication.api.dto.ReminderTaskCreateReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.ReminderTaskRespDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒任务服务 API
 *
 * @author sqlapk
 */
public interface ReminderTaskService {

    /**
     * 创建提醒任务
     *
     * @param createReqDTO 创建请求
     * @return 任务ID
     */
    Long createReminderTask(ReminderTaskCreateReqDTO createReqDTO);

    /**
     * 更新提醒任务
     *
     * @param task 提醒任务
     */
    void updateReminderTask(Object task);

    /**
     * 获取待执行的提醒任务
     *
     * @return 待执行任务列表
     */
    List<ReminderTaskRespDTO> getPendingTasks();

    /**
     * 根据患者ID获取提醒任务
     *
     * @param patientId 患者ID
     * @return 提醒任务列表
     */
    List<ReminderTaskRespDTO> getTasksByPatientId(Long patientId);

    /**
     * 根据今日待办任务ID获取提醒任务
     *
     * @param todayTaskId 今日待办任务ID
     * @return 提醒任务列表
     */
    List<ReminderTaskRespDTO> getTasksByTodayTaskId(Long todayTaskId);

    /**
     * 生成今日提醒任务
     */
    void generateTodayReminderTasks();

}
