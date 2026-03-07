package com.sqlapk.medicine.module.medication.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlapk.medicine.module.medication.api.TodayTaskService;
import com.sqlapk.medicine.module.medication.api.dto.TodayTaskCompleteReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.TodayTaskRespDTO;
import com.sqlapk.medicine.module.medication.convert.TodayTaskConvert;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedPlanDO;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedPlanItemDO;
import com.sqlapk.medicine.module.medication.dal.dataobject.TodayTaskDO;
import com.sqlapk.medicine.module.medication.dal.mysql.MedPlanItemMapper;
import com.sqlapk.medicine.module.medication.dal.mysql.MedPlanMapper;
import com.sqlapk.medicine.module.medication.dal.mysql.TodayTaskMapper;
import com.sqlapk.medicine.module.medication.enums.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 今日待办服务实现
 *
 * @author sqlapk
 */
@Service
@Slf4j
public class TodayTaskServiceImpl implements TodayTaskService {

    @Autowired
    private TodayTaskMapper todayTaskMapper;

    @Autowired
    private MedPlanMapper medPlanMapper;

    @Autowired
    private MedPlanItemMapper medPlanItemMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public void generateTodayTasks(Long planId) {
        log.info("生成今日待办任务，计划ID: {}", planId);

        // 1. 获取计划项
        List<MedPlanItemDO> planItems = medPlanItemMapper.selectByPlanId(planId);

        // 2. 生成今日任务
        LocalDate today = LocalDate.now();
        for (MedPlanItemDO planItem : planItems) {
            generateTasksForPlanItem(planItem, today);
        }

        log.info("今日待办任务生成完成，计划ID: {}", planId);
    }

    @Override
    @Transactional
    public void regenerateTodayTasks(Long planId) {
        log.info("重新生成今日待办任务，计划ID: {}", planId);

        // 1. 删除原有任务
        todayTaskMapper.deleteByPlanId(planId);

        // 2. 重新生成任务
        generateTodayTasks(planId);

        log.info("今日待办任务重新生成完成，计划ID: {}", planId);
    }

    @Override
    public List<TodayTaskRespDTO> getTodayTasksByPatientIdAndDate(Long patientId, LocalDate taskDate) {
        log.info("获取今日待办任务，患者ID: {}, 任务日期: {}", patientId, taskDate);

        List<TodayTaskDO> tasks = todayTaskMapper.selectByPatientIdAndTaskDate(patientId, taskDate);
        List<TodayTaskRespDTO> respDTOs = TodayTaskConvert.INSTANCE.convert(tasks);

        // 填充药品信息
        for (TodayTaskRespDTO respDTO : respDTOs) {
            MedPlanItemDO planItem = medPlanItemMapper.selectById(respDTO.getPlanItemId());
            if (planItem != null) {
                respDTO.setDrugName(planItem.getDrugName());
                respDTO.setDosage(planItem.getDosage());
                respDTO.setInstructions(planItem.getInstructions());
                respDTO.setMealRelationName(getMealRelationName(planItem.getMealRelation()));
            }
        }

        return respDTOs;
    }

    @Override
    @Transactional
    public void completeTask(TodayTaskCompleteReqDTO completeReqDTO) {
        log.info("完成待办任务，任务ID: {}", completeReqDTO.getId());

        TodayTaskDO task = todayTaskMapper.selectById(completeReqDTO.getId());
        if (task == null) {
            throw new RuntimeException("待办任务不存在");
        }

        task.setStatus(TaskStatus.COMPLETED.getStatus());
        task.setCompletedTime(completeReqDTO.getCompletedTime() != null ? 
            completeReqDTO.getCompletedTime() : LocalDateTime.now());
        task.setNotes(completeReqDTO.getNotes());

        todayTaskMapper.updateById(task);

        log.info("待办任务完成成功，任务ID: {}", completeReqDTO.getId());
    }

    @Override
    @Transactional
    public void skipTask(Long taskId, String notes) {
        log.info("跳过待办任务，任务ID: {}", taskId);

        TodayTaskDO task = todayTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("待办任务不存在");
        }

        task.setStatus(TaskStatus.SKIPPED.getStatus());
        task.setNotes(notes);

        todayTaskMapper.updateById(task);

        log.info("待办任务跳过成功，任务ID: {}", taskId);
    }

    @Override
    @Transactional
    public void pauseTasksByPlanId(Long planId) {
        log.info("暂停计划相关任务，计划ID: {}", planId);

        // 获取计划项
        List<MedPlanItemDO> planItems = medPlanItemMapper.selectByPlanId(planId);
        for (MedPlanItemDO planItem : planItems) {
            // 暂停相关任务
            List<TodayTaskDO> tasks = todayTaskMapper.selectByPlanItemIdAndTaskDate(planItem.getId(), LocalDate.now());
            for (TodayTaskDO task : tasks) {
                if (task.getStatus().equals(TaskStatus.PENDING.getStatus())) {
                    task.setStatus(TaskStatus.SKIPPED.getStatus());
                    task.setNotes("计划暂停");
                    todayTaskMapper.updateById(task);
                }
            }
        }

        log.info("计划相关任务暂停完成，计划ID: {}", planId);
    }

    @Override
    @Transactional
    public void resumeTasksByPlanId(Long planId) {
        log.info("恢复计划相关任务，计划ID: {}", planId);

        // 重新生成今日任务
        generateTodayTasks(planId);

        log.info("计划相关任务恢复完成，计划ID: {}", planId);
    }

    @Override
    @Transactional
    public void terminateTasksByPlanId(Long planId) {
        log.info("终止计划相关任务，计划ID: {}", planId);

        // 获取计划项
        List<MedPlanItemDO> planItems = medPlanItemMapper.selectByPlanId(planId);
        for (MedPlanItemDO planItem : planItems) {
            // 终止相关任务
            List<TodayTaskDO> tasks = todayTaskMapper.selectByPlanItemIdAndTaskDate(planItem.getId(), LocalDate.now());
            for (TodayTaskDO task : tasks) {
                if (task.getStatus().equals(TaskStatus.PENDING.getStatus())) {
                    task.setStatus(TaskStatus.SKIPPED.getStatus());
                    task.setNotes("计划终止");
                    todayTaskMapper.updateById(task);
                }
            }
        }

        log.info("计划相关任务终止完成，计划ID: {}", planId);
    }

    /**
     * 为计划项生成任务
     */
    private void generateTasksForPlanItem(MedPlanItemDO planItem, LocalDate taskDate) {
        // 获取计划信息以获取患者ID
        MedPlanDO medPlan = medPlanMapper.selectById(planItem.getPlanId());
        if (medPlan == null) {
            log.warn("用药计划不存在，计划ID: {}", planItem.getPlanId());
            return;
        }

        // 解析时间点
        List<LocalTime> timePoints = parseTimePoints(planItem.getTimePoints());

        // 为每个时间点生成任务
        for (LocalTime timePoint : timePoints) {
            TodayTaskDO task = TodayTaskDO.builder()
                .planItemId(planItem.getId())
                .patientId(medPlan.getPatientId())
                .taskDate(taskDate)
                .scheduledTime(timePoint)
                .status(TaskStatus.PENDING.getStatus())
                .build();

            // 检查任务是否已存在
            if (!isTaskExists(planItem.getId(), taskDate, timePoint)) {
                todayTaskMapper.insert(task);
            }
        }
    }

    /**
     * 解析时间点
     */
    private List<LocalTime> parseTimePoints(String timePointsJson) {
        try {
            List<String> timePointStrings = objectMapper.readValue(timePointsJson, new TypeReference<List<String>>() {});
            List<LocalTime> timePoints = new ArrayList<>();
            for (String timePointString : timePointStrings) {
                timePoints.add(LocalTime.parse(timePointString));
            }
            return timePoints;
        } catch (Exception e) {
            log.error("解析时间点失败: {}", timePointsJson, e);
            return new ArrayList<>();
        }
    }

    /**
     * 检查任务是否已存在
     */
    private boolean isTaskExists(Long planItemId, LocalDate taskDate, LocalTime scheduledTime) {
        return todayTaskMapper.selectByPlanItemAndDateTime(planItemId, taskDate, scheduledTime) != null;
    }

    /**
     * 获取餐前后关系名称
     */
    private String getMealRelationName(Integer mealRelation) {
        switch (mealRelation) {
            case 0: return "独立";
            case 1: return "餐前";
            case 2: return "餐后";
            case 3: return "餐中";
            default: return "未知";
        }
    }

}
