package com.sqlapk.medicine.module.medication.service;

import com.sqlapk.medicine.module.medication.api.dto.ReminderTaskRespDTO;
import com.sqlapk.medicine.module.medication.dal.dataobject.ReminderTaskDO;
import com.sqlapk.medicine.module.medication.dal.mysql.ReminderTaskMapper;
import com.sqlapk.medicine.module.medication.enums.ReminderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提醒任务重试管理器
 *
 * @author sqlapk
 */
@Component
@Slf4j
public class ReminderRetryManager {

    @Autowired
    private ReminderNotificationSender notificationSender;

    @Autowired
    private ReminderTaskMapper reminderTaskMapper;

    private final Map<Long, ReminderTaskRespDTO> retryQueue = new ConcurrentHashMap<>();

    /**
     * 添加任务到重试队列
     */
    public void addToRetryQueue(ReminderTaskRespDTO task) {
        if (task.getRetryCount() < getMaxRetryCount()) {
            retryQueue.put(task.getId(), task);
            log.info("任务加入重试队列，任务ID: {}, 重试次数: {}", task.getId(), task.getRetryCount());
        } else {
            log.warn("任务重试次数已达上限，任务ID: {}", task.getId());
            // 更新任务状态为已放弃
            updateTaskStatus(task.getId(), ReminderStatus.ABANDONED, "重试次数已达上限");
        }
    }

    /**
     * 处理重试任务
     */
    public void processRetryTasks() {
        if (retryQueue.isEmpty()) {
            return;
        }

        log.debug("处理重试任务，队列大小: {}", retryQueue.size());

        Iterator<Map.Entry<Long, ReminderTaskRespDTO>> iterator = retryQueue.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, ReminderTaskRespDTO> entry = iterator.next();
            ReminderTaskRespDTO task = entry.getValue();

            if (shouldRetry(task)) {
                retryTask(task);
                iterator.remove();
            }
        }
    }

    /**
     * 判断是否应该重试
     */
    private boolean shouldRetry(ReminderTaskRespDTO task) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastRetryTime = task.getLastRetryTime();

        if (lastRetryTime == null) {
            return true;
        }

        // 根据重试次数计算重试间隔
        int retryIntervalMinutes = calculateRetryInterval(task.getRetryCount());
        return now.isAfter(lastRetryTime.plusMinutes(retryIntervalMinutes));
    }

    /**
     * 重试任务
     */
    private void retryTask(ReminderTaskRespDTO task) {
        try {
            log.info("重试提醒任务，任务ID: {}, 重试次数: {}", task.getId(), task.getRetryCount() + 1);

            // 更新任务状态为执行中
            updateTaskStatus(task.getId(), ReminderStatus.EXECUTING, null);

            // 更新重试信息
            task.setRetryCount(task.getRetryCount() + 1);
            task.setLastRetryTime(LocalDateTime.now());

            // 发送通知
            boolean success = notificationSender.sendNotification(task);

            if (success) {
                log.info("重试成功，任务ID: {}", task.getId());
                // 更新任务状态为成功
                updateTaskStatus(task.getId(), ReminderStatus.SUCCESS, null);
            } else {
                log.warn("重试失败，任务ID: {}", task.getId());
                // 更新任务失败原因
                updateTaskStatus(task.getId(), ReminderStatus.FAILED, "通知发送失败");

                // 如果还有重试机会，重新加入队列
                if (task.getRetryCount() < getMaxRetryCount()) {
                    addToRetryQueue(task);
                } else {
                    // 重试次数已达上限，标记为已放弃
                    updateTaskStatus(task.getId(), ReminderStatus.ABANDONED, "重试次数已达上限");
                }
            }

        } catch (Exception e) {
            log.error("重试任务异常，任务ID: {}", task.getId(), e);
            // 更新任务失败原因
            updateTaskStatus(task.getId(), ReminderStatus.FAILED, "执行异常: " + e.getMessage());
            
            // 如果还有重试机会，重新加入队列
            if (task.getRetryCount() < getMaxRetryCount()) {
                addToRetryQueue(task);
            } else {
                // 重试次数已达上限，标记为已放弃
                updateTaskStatus(task.getId(), ReminderStatus.ABANDONED, "重试次数已达上限");
            }
        }
    }

    /**
     * 计算重试间隔
     */
    private int calculateRetryInterval(int retryCount) {
        // 指数退避：1分钟、2分钟、4分钟、8分钟
        return (int) Math.pow(2, retryCount);
    }

    /**
     * 获取最大重试次数
     */
    private int getMaxRetryCount() {
        return 4; // 最多重试4次
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(Long taskId, ReminderStatus status, String failureReason) {
        try {
            ReminderTaskDO taskDO = reminderTaskMapper.selectById(taskId);
            if (taskDO != null) {
                taskDO.setStatus(status.getStatus());
                if (failureReason != null) {
                    taskDO.setFailureReason(failureReason);
                }
                taskDO.setLastRetryTime(LocalDateTime.now());
                reminderTaskMapper.updateById(taskDO);
                log.debug("更新任务状态成功，任务ID: {}, 状态: {}", taskId, status.getName());
            } else {
                log.warn("任务不存在，任务ID: {}", taskId);
            }
        } catch (Exception e) {
            log.error("更新任务状态异常，任务ID: {}", taskId, e);
        }
    }

}
