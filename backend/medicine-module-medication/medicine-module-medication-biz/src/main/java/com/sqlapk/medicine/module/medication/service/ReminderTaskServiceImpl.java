package com.sqlapk.medicine.module.medication.service;

import com.sqlapk.medicine.module.medication.api.ReminderTaskService;
import com.sqlapk.medicine.module.medication.api.TodayTaskService;
import com.sqlapk.medicine.module.medication.api.dto.ReminderTaskCreateReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.ReminderTaskRespDTO;
import com.sqlapk.medicine.module.medication.api.dto.TodayTaskRespDTO;
import com.sqlapk.medicine.module.medication.convert.ReminderTaskConvert;
import com.sqlapk.medicine.module.medication.dal.dataobject.ReminderConfigDO;
import com.sqlapk.medicine.module.medication.dal.dataobject.ReminderTaskDO;
import com.sqlapk.medicine.module.medication.dal.mysql.ReminderConfigMapper;
import com.sqlapk.medicine.module.medication.dal.mysql.ReminderTaskMapper;
import com.sqlapk.medicine.module.medication.enums.ReminderStatus;
import com.sqlapk.medicine.module.medication.enums.ReminderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒任务服务实现
 *
 * @author sqlapk
 */
@Service
@Slf4j
public class ReminderTaskServiceImpl implements ReminderTaskService {

    @Autowired
    private ReminderTaskMapper reminderTaskMapper;

    @Autowired
    private ReminderConfigMapper reminderConfigMapper;

    @Autowired
    private TodayTaskService todayTaskService;

    @Override
    @Transactional
    public Long createReminderTask(ReminderTaskCreateReqDTO createReqDTO) {
        log.info("创建提醒任务，患者ID: {}, 计划时间: {}", createReqDTO.getPatientId(), createReqDTO.getScheduledTime());

        ReminderTaskDO reminderTask = ReminderTaskConvert.INSTANCE.convert(createReqDTO);
        reminderTask.setStatus(ReminderStatus.PENDING.getStatus());
        reminderTask.setRetryCount(0);

        reminderTaskMapper.insert(reminderTask);

        log.info("提醒任务创建成功，任务ID: {}", reminderTask.getId());
        return reminderTask.getId();
    }

    @Override
    @Transactional
    public void updateReminderTask(Object task) {
        if (task instanceof ReminderTaskDO) {
            ReminderTaskDO reminderTask = (ReminderTaskDO) task;
            reminderTaskMapper.updateById(reminderTask);
            log.debug("提醒任务更新成功，任务ID: {}", reminderTask.getId());
        }
    }

    @Override
    public List<ReminderTaskRespDTO> getPendingTasks() {
        log.debug("获取待执行的提醒任务");

        LocalDateTime currentTime = LocalDateTime.now();
        List<ReminderTaskDO> tasks = reminderTaskMapper.selectPendingTasks(currentTime);
        List<ReminderTaskRespDTO> respDTOs = ReminderTaskConvert.INSTANCE.convert(tasks);

        // 填充类型和状态名称
        for (ReminderTaskRespDTO respDTO : respDTOs) {
            respDTO.setTypeName(getTypeName(respDTO.getType()));
            respDTO.setStatusName(getStatusName(respDTO.getStatus()));
        }

        return respDTOs;
    }

    @Override
    public List<ReminderTaskRespDTO> getTasksByPatientId(Long patientId) {
        log.info("根据患者ID获取提醒任务，患者ID: {}", patientId);

        List<ReminderTaskDO> tasks = reminderTaskMapper.selectByPatientId(patientId);
        List<ReminderTaskRespDTO> respDTOs = ReminderTaskConvert.INSTANCE.convert(tasks);

        // 填充类型和状态名称
        for (ReminderTaskRespDTO respDTO : respDTOs) {
            respDTO.setTypeName(getTypeName(respDTO.getType()));
            respDTO.setStatusName(getStatusName(respDTO.getStatus()));
        }

        return respDTOs;
    }

    @Override
    public List<ReminderTaskRespDTO> getTasksByTodayTaskId(Long todayTaskId) {
        log.info("根据今日待办任务ID获取提醒任务，今日待办任务ID: {}", todayTaskId);

        List<ReminderTaskDO> tasks = reminderTaskMapper.selectByTodayTaskId(todayTaskId);
        List<ReminderTaskRespDTO> respDTOs = ReminderTaskConvert.INSTANCE.convert(tasks);

        // 填充类型和状态名称
        for (ReminderTaskRespDTO respDTO : respDTOs) {
            respDTO.setTypeName(getTypeName(respDTO.getType()));
            respDTO.setStatusName(getStatusName(respDTO.getStatus()));
        }

        return respDTOs;
    }

    @Override
    @Scheduled(cron = "0 0 6 * * ?") // 每天6点执行
    public void generateTodayReminderTasks() {
        log.info("开始生成今日提醒任务");

        LocalDate today = LocalDate.now();

        try {
            // 1. 获取所有启用的用药提醒配置
            List<ReminderConfigDO> configs = reminderConfigMapper.selectEnabledByType(ReminderType.MEDICATION.getType());

            // 2. 为每个配置生成提醒任务
            for (ReminderConfigDO config : configs) {
                generateReminderTasksForConfig(config, today);
            }

            log.info("今日提醒任务生成完成");
        } catch (Exception e) {
            log.error("生成今日提醒任务异常", e);
        }
    }

    /**
     * 为配置生成提醒任务
     */
    private void generateReminderTasksForConfig(ReminderConfigDO config, LocalDate taskDate) {
        try {
            // 获取患者的今日待办任务
            List<TodayTaskRespDTO> todayTasks = todayTaskService.getTodayTasksByPatientIdAndDate(
                config.getPatientId(), taskDate);

            // 为每个待办任务生成提醒任务
            for (TodayTaskRespDTO todayTask : todayTasks) {
                generateReminderTaskForTodayTask(todayTask, config);
            }
        } catch (Exception e) {
            log.error("为配置生成提醒任务异常，患者ID: {}", config.getPatientId(), e);
        }
    }

    /**
     * 为今日待办任务生成提醒任务
     */
    private void generateReminderTaskForTodayTask(TodayTaskRespDTO todayTask, ReminderConfigDO config) {
        try {
            // 计算提醒时间
            LocalDateTime reminderTime = calculateReminderTime(todayTask, config);

            // 检查是否已存在相同的提醒任务
            if (isReminderTaskExists(todayTask.getId(), config.getType())) {
                log.debug("提醒任务已存在，跳过生成，今日待办任务ID: {}", todayTask.getId());
                return;
            }

            // 创建提醒任务
            ReminderTaskCreateReqDTO createReqDTO = new ReminderTaskCreateReqDTO();
            createReqDTO.setTodayTaskId(todayTask.getId());
            createReqDTO.setPatientId(todayTask.getPatientId());
            createReqDTO.setScheduledTime(reminderTime);
            createReqDTO.setType(config.getType());
            createReqDTO.setNotificationContent(buildNotificationContent(todayTask));

            createReminderTask(createReqDTO);

        } catch (Exception e) {
            log.error("为今日待办任务生成提醒任务异常，今日待办任务ID: {}", todayTask.getId(), e);
        }
    }

    /**
     * 计算提醒时间
     */
    private LocalDateTime calculateReminderTime(TodayTaskRespDTO todayTask, ReminderConfigDO config) {
        LocalDateTime taskTime = LocalDateTime.of(todayTask.getTaskDate(), todayTask.getScheduledTime());
        return taskTime.minusMinutes(config.getAdvanceMinutes());
    }

    /**
     * 检查提醒任务是否已存在
     */
    private boolean isReminderTaskExists(Long todayTaskId, Integer type) {
        List<ReminderTaskDO> existingTasks = reminderTaskMapper.selectByTodayTaskId(todayTaskId);
        return existingTasks.stream()
            .anyMatch(task -> task.getType().equals(type));
    }

    /**
     * 构建通知内容
     */
    private String buildNotificationContent(TodayTaskRespDTO todayTask) {
        return String.format("用药提醒：%s %s，请按时服药", 
            todayTask.getDrugName(), todayTask.getDosage());
    }

    /**
     * 获取类型名称
     */
    private String getTypeName(Integer type) {
        ReminderType reminderType = ReminderType.valueOf(type);
        return reminderType != null ? reminderType.getName() : "未知";
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        ReminderStatus reminderStatus = ReminderStatus.valueOf(status);
        return reminderStatus != null ? reminderStatus.getName() : "未知";
    }

}
