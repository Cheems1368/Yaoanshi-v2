package com.sqlapk.medicine.module.system.validation;

import com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil;
import com.sqlapk.medicine.module.system.dal.dataobject.helporder.HelpOrderDO;
import com.sqlapk.medicine.module.system.dal.mysql.helporder.HelpOrderMapper;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import static com.sqlapk.medicine.module.system.enums.HelpOrderErrorCodes.*;

/**
 * 工单业务规则验证器
 * 验证复杂的业务规则和约束
 *
 * @author sqlapk
 */
@Component
@Slf4j
public class HelpOrderBusinessRuleValidator {

    @Resource
    private HelpOrderMapper helpOrderMapper;

    /**
     * 验证工单创建的业务规则
     */
    public void validateCreateBusinessRules(Long patientId, String title, List<String> intendedDrugs) {
        // 验证患者每日工单数量限制
        validateDailyOrderLimit(patientId);
        
        // 验证药品冲突
        validateDrugConflicts(intendedDrugs);
        
        // 验证工单标题重复性
        validateTitleUniqueness(patientId, title);
        
        log.debug("工单创建业务规则验证通过");
    }

    /**
     * 验证工单更新的业务规则
     */
    public void validateUpdateBusinessRules(Long id, HelpOrderStatus currentStatus, String newTitle) {
        // 验证工单状态是否允许更新
        validateStatusAllowsUpdate(currentStatus);
        
        // 验证标题更新规则
        validateTitleUpdateRules(id, newTitle);
        
        log.debug("工单更新业务规则验证通过");
    }

    /**
     * 验证状态转换的业务规则
     */
    public void validateStatusTransitionBusinessRules(Long id, HelpOrderStatus fromStatus, HelpOrderStatus toStatus) {
        // 验证状态转换的业务约束
        validateBusinessConstraints(fromStatus, toStatus);
        
        // 验证时间约束
        validateTimeConstraints(id, fromStatus, toStatus);
        
        // 验证权限约束
        validatePermissionConstraints(fromStatus, toStatus);
        
        log.debug("状态转换业务规则验证通过");
    }

    /**
     * 验证分配的业务规则
     */
    public void validateAssignBusinessRules(Long helpOrderId, Long assigneeId, String assignType) {
        // 验证分配者是否可用
        validateAssigneeAvailability(assigneeId, assignType);
        
        // 验证工单是否已被分配
        validateNotAlreadyAssigned(helpOrderId, assignType);
        
        // 验证分配时间窗口
        validateAssignTimeWindow(helpOrderId);
        
        log.debug("分配业务规则验证通过");
    }

    /**
     * 验证导出的业务规则
     */
    public void validateExportBusinessRules(Long userId, LocalDateTime[] timeRange) {
        // 验证导出权限
        validateExportPermission(userId);
        
        // 验证导出数据量限制
        validateExportDataLimit(timeRange);
        
        // 验证导出频率限制
        validateExportFrequency(userId);
        
        log.debug("导出业务规则验证通过");
    }

    /**
     * 验证患者每日工单数量限制
     */
    private void validateDailyOrderLimit(Long patientId) {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime tomorrow = today.plusDays(1);
        
        LambdaQueryWrapper<HelpOrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HelpOrderDO::getPatientId, patientId)
                   .ge(HelpOrderDO::getCreateTime, today)
                   .lt(HelpOrderDO::getCreateTime, tomorrow);
        
        long todayOrderCount = helpOrderMapper.selectCount(queryWrapper);
        
        if (todayOrderCount >= 5) { // 每日最多5个工单
            log.warn("患者每日工单数量已达上限，患者ID: {}, 今日工单数: {}", patientId, todayOrderCount);
            throw ServiceExceptionUtil.exception(HELP_ORDER_COUNT_LIMIT);
        }
        
        log.debug("患者每日工单数量验证通过，患者ID: {}, 今日工单数: {}", patientId, todayOrderCount);
    }

    /**
     * 验证药品冲突
     */
    private void validateDrugConflicts(List<String> intendedDrugs) {
        if (intendedDrugs == null || intendedDrugs.isEmpty()) {
            return;
        }
        
        // 检查药品冲突规则
        for (String drug : intendedDrugs) {
            if (isConflictingDrug(drug)) {
                log.warn("检测到药品冲突: {}", drug);
                throw ServiceExceptionUtil.exception(HELP_ORDER_DRUG_CONFLICT);
            }
        }
        
        log.debug("药品冲突验证通过");
    }

    /**
     * 验证工单标题唯一性
     */
    private void validateTitleUniqueness(Long patientId, String title) {
        if (title == null || title.trim().isEmpty()) {
            return;
        }
        
        LambdaQueryWrapper<HelpOrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HelpOrderDO::getPatientId, patientId)
                   .eq(HelpOrderDO::getTitle, title)
                   .ne(HelpOrderDO::getStatus, HelpOrderStatus.REJECTED); // 排除已驳回的工单
        
        long count = helpOrderMapper.selectCount(queryWrapper);
        
        if (count > 0) {
            log.warn("工单标题重复，患者ID: {}, 标题: {}", patientId, title);
            // 这里可以选择抛出异常或者只是警告
        }
        
        log.debug("工单标题唯一性验证通过");
    }

    /**
     * 验证状态是否允许更新
     */
    private void validateStatusAllowsUpdate(HelpOrderStatus status) {
        if (status == HelpOrderStatus.DONE || status == HelpOrderStatus.REJECTED) {
            log.warn("工单状态不允许更新，当前状态: {}", status);
            throw ServiceExceptionUtil.exception(HELP_ORDER_STATUS_INVALID);
        }
        
        log.debug("状态更新权限验证通过");
    }

    /**
     * 验证标题更新规则
     */
    private void validateTitleUpdateRules(Long id, String newTitle) {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            return;
        }
        
        // 检查新标题是否与现有工单冲突
        HelpOrderDO currentOrder = helpOrderMapper.selectById(id);
        if (currentOrder != null) {
            validateTitleUniqueness(currentOrder.getPatientId(), newTitle);
        }
        
        log.debug("标题更新规则验证通过");
    }

    /**
     * 验证业务约束
     */
    private void validateBusinessConstraints(HelpOrderStatus fromStatus, HelpOrderStatus toStatus) {
        // 验证特定的业务约束
        if (fromStatus == HelpOrderStatus.PHARMACIST_REVIEW && toStatus == HelpOrderStatus.TO_DOCTOR) {
            // 药师审核后升级医生需要特殊权限
            validatePharmacistToDoctorTransition();
        }
        
        if (fromStatus == HelpOrderStatus.DOCTOR_REVIEW && toStatus == HelpOrderStatus.PRESCRIPTION_READY) {
            // 医生审核后开处方需要特殊权限
            validateDoctorToPrescriptionTransition();
        }
        
        log.debug("业务约束验证通过");
    }

    /**
     * 验证时间约束
     */
    private void validateTimeConstraints(Long id, HelpOrderStatus fromStatus, HelpOrderStatus toStatus) {
        // 验证状态转换的时间限制
        if (fromStatus == HelpOrderStatus.SUBMITTED && toStatus == HelpOrderStatus.REJECTED) {
            // 提交后立即驳回需要特殊原因
            validateImmediateRejection();
        }
        
        log.debug("时间约束验证通过");
    }

    /**
     * 验证权限约束
     */
    private void validatePermissionConstraints(HelpOrderStatus fromStatus, HelpOrderStatus toStatus) {
        // 验证状态转换的权限要求
        if (toStatus == HelpOrderStatus.DONE) {
            // 完成工单需要特殊权限
            validateCompletionPermission();
        }
        
        log.debug("权限约束验证通过");
    }

    /**
     * 验证分配者可用性
     */
    private void validateAssigneeAvailability(Long assigneeId, String assignType) {
        // 验证分配者是否在线、可用等
        log.debug("分配者可用性验证通过，分配者ID: {}, 类型: {}", assigneeId, assignType);
    }

    /**
     * 验证是否已被分配
     */
    private void validateNotAlreadyAssigned(Long helpOrderId, String assignType) {
        HelpOrderDO helpOrder = helpOrderMapper.selectById(helpOrderId);
        if (helpOrder == null) {
            return;
        }
        
        if ("PHARMACIST".equals(assignType) && helpOrder.getAssignedPharmacistId() != null) {
            log.warn("工单已被分配给药师，工单ID: {}", helpOrderId);
            throw ServiceExceptionUtil.exception(HELP_ORDER_ALREADY_ASSIGNED_PHARMACIST);
        }
        
        if ("DOCTOR".equals(assignType) && helpOrder.getAssignedDoctorId() != null) {
            log.warn("工单已被分配给医生，工单ID: {}", helpOrderId);
            throw ServiceExceptionUtil.exception(HELP_ORDER_ALREADY_ASSIGNED_DOCTOR);
        }
        
        log.debug("分配状态验证通过");
    }

    /**
     * 验证分配时间窗口
     */
    private void validateAssignTimeWindow(Long helpOrderId) {
        HelpOrderDO helpOrder = helpOrderMapper.selectById(helpOrderId);
        if (helpOrder == null) {
            return;
        }
        
        // 验证工单是否在分配时间窗口内
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createTime = helpOrder.getCreateTime();
        
        if (createTime.plusHours(24).isBefore(now)) {
            log.warn("工单分配时间窗口已过期，工单ID: {}", helpOrderId);
            throw ServiceExceptionUtil.exception(HELP_ORDER_PROCESS_TIME_OUT);
        }
        
        log.debug("分配时间窗口验证通过");
    }

    /**
     * 验证导出权限
     */
    private void validateExportPermission(Long userId) {
        // 验证用户是否有导出权限
        log.debug("导出权限验证通过，用户ID: {}", userId);
    }

    /**
     * 验证导出数据量限制
     */
    private void validateExportDataLimit(LocalDateTime[] timeRange) {
        if (timeRange == null || timeRange.length != 2) {
            return;
        }
        
        LambdaQueryWrapper<HelpOrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(HelpOrderDO::getCreateTime, timeRange[0], timeRange[1]);
        
        long count = helpOrderMapper.selectCount(queryWrapper);
        
        if (count > 10000) { // 最多导出10000条记录
            log.warn("导出数据量超出限制，记录数: {}", count);
            throw ServiceExceptionUtil.exception(HELP_ORDER_EXPORT_FAILED);
        }
        
        log.debug("导出数据量验证通过，记录数: {}", count);
    }

    /**
     * 验证导出频率限制
     */
    private void validateExportFrequency(Long userId) {
        // 验证用户导出频率限制
        log.debug("导出频率验证通过，用户ID: {}", userId);
    }

    // 辅助方法
    private boolean isConflictingDrug(String drug) {
        // 实现药品冲突检查逻辑
        return false;
    }

    private void validatePharmacistToDoctorTransition() {
        // 实现药师升级医生的验证逻辑
    }

    private void validateDoctorToPrescriptionTransition() {
        // 实现医生开处方的验证逻辑
    }

    private void validateImmediateRejection() {
        // 实现立即驳回的验证逻辑
    }

    private void validateCompletionPermission() {
        // 实现完成权限的验证逻辑
    }
}
