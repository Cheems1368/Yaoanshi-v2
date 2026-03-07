package com.sqlapk.medicine.module.medication.job;

import com.sqlapk.medicine.module.medication.api.TodayTaskService;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedPlanDO;
import com.sqlapk.medicine.module.medication.dal.mysql.MedPlanMapper;
import com.sqlapk.medicine.module.medication.enums.MedPlanStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日任务生成定时任务
 * 
 * 每日 00:05 生成当日全量 Todo
 *
 * @author sqlapk
 */
@Component
@Slf4j
public class DailyTaskGenerationJob {

    @Autowired
    private MedPlanMapper medPlanMapper;

    @Autowired
    private TodayTaskService todayTaskService;

    /**
     * 每日生成待办任务
     * 执行时间：每日 00:05
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void generateDailyTasks() {
        log.info("开始执行每日任务生成定时任务");
        
        try {
            LocalDate today = LocalDate.now();
            log.info("生成日期: {}", today);

            // 1. 获取所有活跃的用药计划
            List<MedPlanDO> activePlans = medPlanMapper.selectByStatusAndDateRange(
                MedPlanStatus.ACTIVE.getStatus(), 
                today, 
                today.plusDays(30) // 提前生成30天的任务
            );

            log.info("找到 {} 个活跃的用药计划", activePlans.size());

            // 2. 为每个计划生成今日任务
            int successCount = 0;
            int failCount = 0;
            
            for (MedPlanDO plan : activePlans) {
                try {
                    // 检查计划是否在有效期内
                    if (isPlanValidForDate(plan, today)) {
                        todayTaskService.generateTodayTasks(plan.getId());
                        successCount++;
                        log.debug("为计划 {} 生成任务成功", plan.getId());
                    } else {
                        log.debug("计划 {} 不在有效期内，跳过", plan.getId());
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("为计划 {} 生成任务失败", plan.getId(), e);
                }
            }

            log.info("每日任务生成完成，成功: {}, 失败: {}", successCount, failCount);

        } catch (Exception e) {
            log.error("每日任务生成定时任务执行失败", e);
        }
    }

    /**
     * 检查计划是否在指定日期有效
     */
    private boolean isPlanValidForDate(MedPlanDO plan, LocalDate date) {
        // 检查开始日期
        if (plan.getStartDate().isAfter(date)) {
            return false;
        }

        // 检查结束日期
        if (plan.getEndDate() != null && plan.getEndDate().isBefore(date)) {
            return false;
        }

        return true;
    }

    /**
     * 手动触发任务生成（用于测试）
     */
    public void manualGenerateTasks() {
        log.info("手动触发任务生成");
        generateDailyTasks();
    }

    /**
     * 为指定计划生成任务（用于事件驱动）
     */
    public void generateTasksForPlan(Long planId) {
        log.info("为指定计划生成任务，计划ID: {}", planId);
        
        try {
            MedPlanDO plan = medPlanMapper.selectById(planId);
            if (plan == null) {
                log.warn("计划不存在，计划ID: {}", planId);
                return;
            }

            if (!MedPlanStatus.ACTIVE.equals(MedPlanStatus.valueOf(plan.getStatus()))) {
                log.warn("计划状态不是活跃状态，计划ID: {}, 状态: {}", planId, plan.getStatus());
                return;
            }

            todayTaskService.generateTodayTasks(planId);
            log.info("为计划 {} 生成任务成功", planId);

        } catch (Exception e) {
            log.error("为计划 {} 生成任务失败", planId, e);
        }
    }

}
