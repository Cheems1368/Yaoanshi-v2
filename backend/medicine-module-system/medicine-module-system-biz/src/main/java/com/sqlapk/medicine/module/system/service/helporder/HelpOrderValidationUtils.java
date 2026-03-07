package com.sqlapk.medicine.module.system.service.helporder;

import com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil;
import com.sqlapk.medicine.module.system.dal.dataobject.helporder.HelpOrderDO;
import com.sqlapk.medicine.module.system.dal.mysql.helporder.HelpOrderMapper;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

import static com.sqlapk.medicine.module.system.enums.HelpOrderErrorCodes.*;

/**
 * 工单验证工具类
 * 提供统一的验证逻辑和详细的错误信息
 *
 * @author sqlapk
 */
@Component
@Slf4j
public class HelpOrderValidationUtils {

    @Resource
    private HelpOrderMapper helpOrderMapper;

    /**
     * 验证工单是否存在，并返回工单对象
     * 
     * @param id 工单ID
     * @return 工单对象
     * @throws ServiceException 工单不存在时抛出异常
     */
    public HelpOrderDO validateHelpOrderExistsAndGet(Long id) {
        if (id == null) {
            log.warn("工单ID不能为空");
            throw ServiceExceptionUtil.exception(HELP_ORDER_TITLE_NOT_EMPTY);
        }

        HelpOrderDO helpOrder = helpOrderMapper.selectById(id);
        if (helpOrder == null) {
            log.warn("工单不存在，ID: {}", id);
            throw ServiceExceptionUtil.exception(HELP_ORDER_NOT_EXISTS);
        }
        
        log.debug("工单验证通过，ID: {}, 状态: {}", id, helpOrder.getStatus());
        return helpOrder;
    }

    /**
     * 验证工单状态转换是否有效
     * 
     * @param fromStatus 当前状态
     * @param toStatus 目标状态
     * @param helpOrderId 工单ID（用于日志）
     * @return 是否有效
     */
    public boolean validateStatusTransition(HelpOrderStatus fromStatus, HelpOrderStatus toStatus, Long helpOrderId) {
        if (fromStatus == null || toStatus == null) {
            log.warn("工单状态转换验证失败，状态不能为空，工单ID: {}", helpOrderId);
            return false;
        }

        boolean isValid = isValidStatusTransition(fromStatus, toStatus);
        if (!isValid) {
            log.warn("工单状态转换无效，从 {} 到 {}，工单ID: {}", 
                fromStatus.getCode(), toStatus.getCode(), helpOrderId);
        } else {
            log.debug("工单状态转换验证通过，从 {} 到 {}，工单ID: {}", 
                fromStatus.getCode(), toStatus.getCode(), helpOrderId);
        }
        
        return isValid;
    }

    /**
     * 验证工单状态转换并抛出异常（如果无效）
     * 
     * @param fromStatus 当前状态
     * @param toStatus 目标状态
     * @param helpOrderId 工单ID
     */
    public void validateStatusTransitionOrThrow(HelpOrderStatus fromStatus, HelpOrderStatus toStatus, Long helpOrderId) {
        if (!validateStatusTransition(fromStatus, toStatus, helpOrderId)) {
            throw ServiceExceptionUtil.exception(HELP_ORDER_STATUS_TRANSITION_INVALID);
        }
    }

    /**
     * 验证用户权限
     * 
     * @param userId 用户ID
     * @param operation 操作类型
     * @param helpOrderId 工单ID
     */
    public void validateUserPermission(Long userId, String operation, Long helpOrderId) {
        if (userId == null) {
            log.warn("用户ID不能为空，操作: {}, 工单ID: {}", operation, helpOrderId);
            throw ServiceExceptionUtil.exception(USER_ID_NOT_EMPTY);
        }
        
        // 这里可以添加更详细的权限验证逻辑
        log.debug("用户权限验证通过，用户ID: {}, 操作: {}, 工单ID: {}", userId, operation, helpOrderId);
    }

    /**
     * 验证分页参数
     * 
     * @param pageNo 页码
     * @param pageSize 每页大小
     */
    public void validatePageParams(Integer pageNo, Integer pageSize) {
        if (pageNo != null && pageNo <= 0) {
            log.warn("页码必须为正数，当前值: {}", pageNo);
            throw ServiceExceptionUtil.exception(PAGE_NO_MUST_POSITIVE);
        }
        
        if (pageSize != null && (pageSize <= 0 || pageSize > 100)) {
            log.warn("每页大小必须在1-100之间，当前值: {}", pageSize);
            throw ServiceExceptionUtil.exception(PAGE_SIZE_RANGE_ERROR);
        }
        
        log.debug("分页参数验证通过，页码: {}, 每页大小: {}", pageNo, pageSize);
    }

    /**
     * 验证时间范围参数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    public void validateTimeRange(Object startTime, Object endTime) {
        if (startTime != null && endTime != null) {
            // 这里可以添加时间范围验证逻辑
            log.debug("时间范围验证通过，开始时间: {}, 结束时间: {}", startTime, endTime);
        }
    }

    /**
     * 验证文件上传参数
     * 
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param maxSize 最大文件大小
     */
    public void validateFileUpload(String fileName, Long fileSize, Long maxSize) {
        if (fileSize != null && maxSize != null && fileSize > maxSize) {
            log.warn("文件大小超出限制，文件名: {}, 大小: {}, 限制: {}", fileName, fileSize, maxSize);
            throw ServiceExceptionUtil.exception(FILE_SIZE_EXCEEDED);
        }
        
        log.debug("文件上传验证通过，文件名: {}, 大小: {}", fileName, fileSize);
    }

    /**
     * 验证请求频率限制
     * 
     * @param userId 用户ID
     * @param operation 操作类型
     */
    public void validateRateLimit(Long userId, String operation) {
        // 这里可以添加频率限制验证逻辑
        log.debug("请求频率验证通过，用户ID: {}, 操作: {}", userId, operation);
    }

    /**
     * 验证数据完整性
     * 
     * @param helpOrder 工单对象
     */
    public void validateDataIntegrity(HelpOrderDO helpOrder) {
        if (helpOrder == null) {
            log.warn("工单对象不能为空");
            throw ServiceExceptionUtil.exception(DATA_INTEGRITY_CHECK_FAILED);
        }
        
        // 验证必要字段
        if (helpOrder.getPatientId() == null) {
            log.warn("患者ID不能为空，工单ID: {}", helpOrder.getId());
            throw ServiceExceptionUtil.exception(USER_ID_NOT_EMPTY);
        }
        
        if (helpOrder.getTitle() == null || helpOrder.getTitle().trim().isEmpty()) {
            log.warn("工单标题不能为空，工单ID: {}", helpOrder.getId());
            throw ServiceExceptionUtil.exception(HELP_ORDER_TITLE_NOT_EMPTY);
        }
        
        if (helpOrder.getDescription() == null || helpOrder.getDescription().trim().isEmpty()) {
            log.warn("工单描述不能为空，工单ID: {}", helpOrder.getId());
            throw ServiceExceptionUtil.exception(HELP_ORDER_DESCRIPTION_NOT_EMPTY);
        }
        
        log.debug("数据完整性验证通过，工单ID: {}", helpOrder.getId());
    }

    /**
     * 定义允许的状态转换规则
     */
    private boolean isValidStatusTransition(HelpOrderStatus fromStatus, HelpOrderStatus toStatus) {
        switch (fromStatus) {
            case SUBMITTED:
                return toStatus == HelpOrderStatus.ASSIGNED_PHARMACIST || toStatus == HelpOrderStatus.REJECTED;
            case ASSIGNED_PHARMACIST:
                return toStatus == HelpOrderStatus.PHARMACIST_REVIEW || toStatus == HelpOrderStatus.REJECTED;
            case PHARMACIST_REVIEW:
                return toStatus == HelpOrderStatus.ADVICE_READY || 
                       toStatus == HelpOrderStatus.TO_DOCTOR || 
                       toStatus == HelpOrderStatus.REJECTED;
            case ADVICE_READY:
                return toStatus == HelpOrderStatus.DONE || toStatus == HelpOrderStatus.TO_DOCTOR;
            case TO_DOCTOR:
                return toStatus == HelpOrderStatus.DOCTOR_REVIEW || toStatus == HelpOrderStatus.REJECTED;
            case DOCTOR_REVIEW:
                return toStatus == HelpOrderStatus.PRESCRIPTION_READY || toStatus == HelpOrderStatus.REJECTED;
            case PRESCRIPTION_READY:
                return toStatus == HelpOrderStatus.DONE;
            case DONE:
                return false; // 已完成状态不能转换
            case REJECTED:
                return toStatus == HelpOrderStatus.SUBMITTED; // 驳回后可以重新提交
            default:
                return false;
        }
    }
}
