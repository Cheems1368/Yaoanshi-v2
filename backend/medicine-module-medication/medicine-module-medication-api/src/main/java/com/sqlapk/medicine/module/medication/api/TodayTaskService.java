package com.sqlapk.medicine.module.medication.api;

import com.sqlapk.medicine.module.medication.api.dto.TodayTaskCompleteReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.TodayTaskRespDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 今日待办服务 API
 *
 * @author sqlapk
 */
public interface TodayTaskService {

    /**
     * 生成今日待办任务
     *
     * @param planId 计划ID
     */
    void generateTodayTasks(Long planId);

    /**
     * 重新生成今日待办任务
     *
     * @param planId 计划ID
     */
    void regenerateTodayTasks(Long planId);

    /**
     * 根据患者ID和日期获取今日待办任务
     *
     * @param patientId 患者ID
     * @param taskDate 任务日期
     * @return 今日待办任务列表
     */
    List<TodayTaskRespDTO> getTodayTasksByPatientIdAndDate(Long patientId, LocalDate taskDate);

    /**
     * 完成待办任务
     *
     * @param completeReqDTO 完成请求
     */
    void completeTask(TodayTaskCompleteReqDTO completeReqDTO);

    /**
     * 跳过待办任务
     *
     * @param taskId 任务ID
     * @param notes 备注
     */
    void skipTask(Long taskId, String notes);

    /**
     * 暂停计划相关任务
     *
     * @param planId 计划ID
     */
    void pauseTasksByPlanId(Long planId);

    /**
     * 恢复计划相关任务
     *
     * @param planId 计划ID
     */
    void resumeTasksByPlanId(Long planId);

    /**
     * 终止计划相关任务
     *
     * @param planId 计划ID
     */
    void terminateTasksByPlanId(Long planId);

}
