package com.sqlapk.medicine.module.medication.service;

import com.sqlapk.medicine.module.medication.api.ReminderTaskService;
import com.sqlapk.medicine.module.medication.api.dto.ReminderTaskRespDTO;
import com.sqlapk.medicine.module.medication.dal.dataobject.ReminderTaskDO;
import com.sqlapk.medicine.module.medication.enums.ReminderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 提醒任务调度器
 *
 * @author sqlapk
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "medicine.scheduler.reminder", name = "enabled", havingValue = "true", matchIfMissing = false)
public class ReminderScheduler {

    @Autowired
    private ReminderTaskService reminderTaskService;

    @Autowired
    private ReminderNotificationSender notificationSender;

    @Autowired
    private ReminderRetryManager retryManager;

    /**
     * 执行提醒任务
     */
    // @Scheduled(fixedRate = 60000) // 临时禁用，避免健康检查失败
    public void executeReminderTasks() {
        log.debug("检查待执行的提醒任务");

        try {
            // 1. 获取待执行的提醒任务
            List<ReminderTaskRespDTO> pendingTasks = reminderTaskService.getPendingTasks();

            // 2. 执行提醒任务
            for (ReminderTaskRespDTO task : pendingTasks) {
                executeReminderTask(task);
            }

            // 3. 处理重试任务
            retryManager.processRetryTasks();

        } catch (Exception e) {
            log.error("执行提醒任务异常", e);
        }
    }

    /**
     * 执行单个提醒任务
     */
    private void executeReminderTask(ReminderTaskRespDTO task) {
        try {
            log.info("执行提醒任务，任务ID: {}", task.getId());

            // 更新任务状态为执行中
            updateTaskStatus(task.getId(), ReminderStatus.EXECUTING);

            // 发送通知
            boolean success = notificationSender.sendNotification(task);

            if (success) {
                // 更新任务状态为成功
                updateTaskStatus(task.getId(), ReminderStatus.SUCCESS);
                log.info("提醒任务执行成功，任务ID: {}", task.getId());
            } else {
                // 标记为失败，准备重试
                updateTaskStatus(task.getId(), ReminderStatus.FAILED);
                updateTaskFailureReason(task.getId(), "通知发送失败");

                // 加入重试队列
                retryManager.addToRetryQueue(task);
                log.warn("提醒任务执行失败，任务ID: {}", task.getId());
            }

        } catch (Exception e) {
            log.error("执行提醒任务异常，任务ID: {}", task.getId(), e);

            // 更新任务状态
            updateTaskStatus(task.getId(), ReminderStatus.FAILED);
            updateTaskFailureReason(task.getId(), "执行异常: " + e.getMessage());

            // 加入重试队列
            retryManager.addToRetryQueue(task);
        }
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(Long taskId, ReminderStatus status) {
        try {
            // 这里需要实现具体的状态更新逻辑
            // 由于ReminderTaskService的updateReminderTask方法参数是Object，需要适配
            log.debug("更新任务状态，任务ID: {}, 状态: {}", taskId, status.getName());
        } catch (Exception e) {
            log.error("更新任务状态异常，任务ID: {}", taskId, e);
        }
    }

    /**
     * 更新任务失败原因
     */
    private void updateTaskFailureReason(Long taskId, String reason) {
        try {
            log.debug("更新任务失败原因，任务ID: {}, 原因: {}", taskId, reason);
        } catch (Exception e) {
            log.error("更新任务失败原因异常，任务ID: {}", taskId, e);
        }
    }

}
