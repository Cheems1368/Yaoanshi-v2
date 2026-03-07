package com.sqlapk.medicine.module.medication.service;

import com.sqlapk.medicine.framework.common.exception.ServiceException;
import com.sqlapk.medicine.module.medication.api.TodayTaskService;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedPlanDO;
import com.sqlapk.medicine.module.medication.dal.mysql.MedPlanMapper;
import com.sqlapk.medicine.module.medication.enums.MedPlanStatus;
import com.sqlapk.medicine.module.medication.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 用药计划状态机服务
 * 
 * 管理用药计划的状态流转：DRAFT → ACTIVE → PAUSED → DONE/CANCELLED
 *
 * @author sqlapk
 */
@Service
@Slf4j
public class MedPlanStateMachineService {

    @Autowired
    private MedPlanMapper medPlanMapper;

    @Autowired
    private TodayTaskService todayTaskService;

    /**
     * 激活用药计划
     * 状态流转：DRAFT → ACTIVE
     */
    @Transactional
    public void activateMedPlan(Long planId) {
        log.info("激活用药计划，计划ID: {}", planId);
        
        MedPlanDO medPlan = medPlanMapper.selectById(planId);
        if (medPlan == null) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_NOT_EXISTS);
        }

        // 状态校验
        if (!MedPlanStatus.DRAFT.equals(MedPlanStatus.valueOf(medPlan.getStatus()))) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_STATUS_ERROR);
        }

        // 日期校验
        if (medPlan.getStartDate().isAfter(LocalDate.now())) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_DATE_INVALID);
        }

        // 更新状态
        medPlan.setStatus(MedPlanStatus.ACTIVE.getStatus());
        medPlan.setVersion(medPlan.getVersion() + 1);
        medPlanMapper.updateById(medPlan);

        // 生成今日待办任务
        todayTaskService.generateTodayTasks(planId);

        log.info("用药计划激活成功，计划ID: {}", planId);
    }

    /**
     * 暂停用药计划
     * 状态流转：ACTIVE → PAUSED
     */
    @Transactional
    public void pauseMedPlan(Long planId) {
        log.info("暂停用药计划，计划ID: {}", planId);
        
        MedPlanDO medPlan = medPlanMapper.selectById(planId);
        if (medPlan == null) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_NOT_EXISTS);
        }

        // 状态校验
        if (!MedPlanStatus.ACTIVE.equals(MedPlanStatus.valueOf(medPlan.getStatus()))) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_STATUS_ERROR);
        }

        // 更新状态
        medPlan.setStatus(MedPlanStatus.PAUSED.getStatus());
        medPlan.setVersion(medPlan.getVersion() + 1);
        medPlanMapper.updateById(medPlan);

        // 暂停相关任务
        todayTaskService.pauseTasksByPlanId(planId);

        log.info("用药计划暂停成功，计划ID: {}", planId);
    }

    /**
     * 恢复用药计划
     * 状态流转：PAUSED → ACTIVE
     */
    @Transactional
    public void resumeMedPlan(Long planId) {
        log.info("恢复用药计划，计划ID: {}", planId);
        
        MedPlanDO medPlan = medPlanMapper.selectById(planId);
        if (medPlan == null) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_NOT_EXISTS);
        }

        // 状态校验
        if (!MedPlanStatus.PAUSED.equals(MedPlanStatus.valueOf(medPlan.getStatus()))) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_STATUS_ERROR);
        }

        // 更新状态
        medPlan.setStatus(MedPlanStatus.ACTIVE.getStatus());
        medPlan.setVersion(medPlan.getVersion() + 1);
        medPlanMapper.updateById(medPlan);

        // 恢复相关任务
        todayTaskService.resumeTasksByPlanId(planId);

        log.info("用药计划恢复成功，计划ID: {}", planId);
    }

    /**
     * 终止用药计划
     * 状态流转：ACTIVE/PAUSED → TERMINATED
     */
    @Transactional
    public void terminateMedPlan(Long planId) {
        log.info("终止用药计划，计划ID: {}", planId);
        
        MedPlanDO medPlan = medPlanMapper.selectById(planId);
        if (medPlan == null) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_NOT_EXISTS);
        }

        // 状态校验
        MedPlanStatus currentStatus = MedPlanStatus.valueOf(medPlan.getStatus());
        if (!MedPlanStatus.ACTIVE.equals(currentStatus) && !MedPlanStatus.PAUSED.equals(currentStatus)) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_STATUS_ERROR);
        }

        // 更新状态
        medPlan.setStatus(MedPlanStatus.TERMINATED.getStatus());
        medPlan.setVersion(medPlan.getVersion() + 1);
        medPlanMapper.updateById(medPlan);

        // 终止相关任务
        todayTaskService.terminateTasksByPlanId(planId);

        log.info("用药计划终止成功，计划ID: {}", planId);
    }

    /**
     * 完成用药计划
     * 状态流转：ACTIVE → COMPLETED
     */
    @Transactional
    public void completeMedPlan(Long planId) {
        log.info("完成用药计划，计划ID: {}", planId);
        
        MedPlanDO medPlan = medPlanMapper.selectById(planId);
        if (medPlan == null) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_NOT_EXISTS);
        }

        // 状态校验
        if (!MedPlanStatus.ACTIVE.equals(MedPlanStatus.valueOf(medPlan.getStatus()))) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_STATUS_ERROR);
        }

        // 更新状态
        medPlan.setStatus(MedPlanStatus.COMPLETED.getStatus());
        medPlan.setVersion(medPlan.getVersion() + 1);
        medPlanMapper.updateById(medPlan);

        // 完成相关任务
        todayTaskService.terminateTasksByPlanId(planId);

        log.info("用药计划完成成功，计划ID: {}", planId);
    }

    /**
     * 检查状态流转是否合法
     */
    public boolean isValidTransition(MedPlanStatus from, MedPlanStatus to) {
        switch (from) {
            case DRAFT:
                return MedPlanStatus.ACTIVE.equals(to) || MedPlanStatus.TERMINATED.equals(to);
            case ACTIVE:
                return MedPlanStatus.PAUSED.equals(to) || MedPlanStatus.TERMINATED.equals(to) || MedPlanStatus.COMPLETED.equals(to);
            case PAUSED:
                return MedPlanStatus.ACTIVE.equals(to) || MedPlanStatus.TERMINATED.equals(to);
            case TERMINATED:
            case COMPLETED:
                return false; // 终态，不允许再流转
            default:
                return false;
        }
    }

    /**
     * 获取状态流转描述
     */
    public String getTransitionDescription(MedPlanStatus from, MedPlanStatus to) {
        if (!isValidTransition(from, to)) {
            return "无效的状态流转";
        }

        switch (from) {
            case DRAFT:
                if (MedPlanStatus.ACTIVE.equals(to)) {
                    return "草稿 → 活跃";
                } else if (MedPlanStatus.TERMINATED.equals(to)) {
                    return "草稿 → 终止";
                }
                break;
            case ACTIVE:
                if (MedPlanStatus.PAUSED.equals(to)) {
                    return "活跃 → 暂停";
                } else if (MedPlanStatus.TERMINATED.equals(to)) {
                    return "活跃 → 终止";
                } else if (MedPlanStatus.COMPLETED.equals(to)) {
                    return "活跃 → 完成";
                }
                break;
            case PAUSED:
                if (MedPlanStatus.ACTIVE.equals(to)) {
                    return "暂停 → 活跃";
                } else if (MedPlanStatus.TERMINATED.equals(to)) {
                    return "暂停 → 终止";
                }
                break;
            case COMPLETED:
            case TERMINATED:
                return "终态，不允许流转";
        }
        return "未知状态流转";
    }

}
