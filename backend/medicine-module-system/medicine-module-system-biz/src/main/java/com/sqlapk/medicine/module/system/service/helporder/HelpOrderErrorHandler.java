package com.sqlapk.medicine.module.system.service.helporder;

import com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil;
import com.sqlapk.medicine.framework.common.exception.ServiceException;
import com.sqlapk.medicine.module.system.enums.HelpOrderErrorCodes;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工单错误处理增强器
 * 提供详细的错误信息、错误统计和错误恢复建议
 *
 * @author sqlapk
 */
@Component
@Slf4j
public class HelpOrderErrorHandler {

    /**
     * 错误统计计数器
     */
    private final Map<String, AtomicInteger> errorCounters = new ConcurrentHashMap<>();

    /**
     * 处理工单创建错误
     *
     * @param patientId 患者ID
     * @param error 异常
     * @return 增强的错误信息
     */
    public ServiceException handleCreateError(Long patientId, Exception error) {
        String errorKey = "CREATE_ERROR_" + error.getClass().getSimpleName();
        incrementErrorCounter(errorKey);
        
        log.error("工单创建失败 - 患者ID: {}, 错误类型: {}, 错误信息: {}", 
                patientId, error.getClass().getSimpleName(), error.getMessage());
        
        // 如果是数据库语法错误（如表不存在），直接返回系统级错误，避免误导用户
        if (error instanceof BadSqlGrammarException ||
                (error.getMessage() != null && error.getMessage().contains("doesn't exist"))) {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_SYSTEM_ERROR);
        }

        // 根据错误类型返回具体的错误码
        String message = error.getMessage();
        if (message != null && message.contains("title")) {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_TITLE_NOT_EMPTY);
        } else if (message != null && message.contains("description")) {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_DESCRIPTION_NOT_EMPTY);
        } else if (message != null && message.contains("drugs")) {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_INTENDED_DRUGS_TOO_MANY);
        } else if (message != null && message.contains("attachments")) {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_ATTACHMENTS_TOO_MANY);
        } else {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_CREATE_FAILED);
        }
    }

    /**
     * 处理工单更新错误
     *
     * @param orderId 工单ID
     * @param error 异常
     * @return 增强的错误信息
     */
    public ServiceException handleUpdateError(Long orderId, Exception error) {
        String errorKey = "UPDATE_ERROR_" + error.getClass().getSimpleName();
        incrementErrorCounter(errorKey);
        
        log.error("工单更新失败 - 工单ID: {}, 错误类型: {}, 错误信息: {}", 
                orderId, error.getClass().getSimpleName(), error.getMessage());
        
        if (error.getMessage().contains("not found") || error.getMessage().contains("不存在")) {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_NOT_EXISTS);
        } else {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_UPDATE_FAILED);
        }
    }

    /**
     * 处理状态转换错误
     *
     * @param orderId 工单ID
     * @param fromStatus 原状态
     * @param toStatus 目标状态
     * @param error 异常
     * @return 增强的错误信息
     */
    public ServiceException handleStatusTransitionError(
            Long orderId, HelpOrderStatus fromStatus, HelpOrderStatus toStatus, Exception error) {
        String errorKey = "STATUS_TRANSITION_ERROR_" + fromStatus + "_TO_" + toStatus;
        incrementErrorCounter(errorKey);
        
        log.error("工单状态转换失败 - 工单ID: {}, 从状态: {} 到状态: {}, 错误信息: {}", 
                orderId, fromStatus, toStatus, error.getMessage());
        
        return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_STATUS_TRANSITION_INVALID);
    }

    /**
     * 处理分配错误
     *
     * @param orderId 工单ID
     * @param assigneeType 分配类型（PHARMACIST/DOCTOR）
     * @param assigneeId 分配人ID
     * @param error 异常
     * @return 增强的错误信息
     */
    public ServiceException handleAssignError(
            Long orderId, String assigneeType, Long assigneeId, Exception error) {
        String errorKey = "ASSIGN_ERROR_" + assigneeType;
        incrementErrorCounter(errorKey);
        
        log.error("工单分配失败 - 工单ID: {}, 分配类型: {}, 分配人ID: {}, 错误信息: {}", 
                orderId, assigneeType, assigneeId, error.getMessage());
        
        if ("PHARMACIST".equals(assigneeType)) {
            if (error.getMessage().contains("not found") || error.getMessage().contains("不存在")) {
                return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_PHARMACIST_NOT_EXISTS);
            } else {
                return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_ALREADY_ASSIGNED_PHARMACIST);
            }
        } else if ("DOCTOR".equals(assigneeType)) {
            if (error.getMessage().contains("not found") || error.getMessage().contains("不存在")) {
                return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_DOCTOR_NOT_EXISTS);
            } else {
                return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_ALREADY_ASSIGNED_DOCTOR);
            }
        } else {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_ALREADY_ASSIGNED);
        }
    }

    /**
     * 处理查询错误
     *
     * @param queryType 查询类型
     * @param error 异常
     * @return 增强的错误信息
     */
    public ServiceException handleQueryError(String queryType, Exception error) {
        String errorKey = "QUERY_ERROR_" + queryType;
        incrementErrorCounter(errorKey);
        
        log.error("工单查询失败 - 查询类型: {}, 错误信息: {}", queryType, error.getMessage());
        
        if (queryType.contains("page")) {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.PAGE_SIZE_RANGE_ERROR);
        } else if (queryType.contains("export")) {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_EXPORT_FAILED);
        } else {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_SYSTEM_ERROR);
        }
    }

    /**
     * 处理文件操作错误
     *
     * @param operation 操作类型
     * @param fileName 文件名
     * @param error 异常
     * @return 增强的错误信息
     */
    public ServiceException handleFileError(String operation, String fileName, Exception error) {
        String errorKey = "FILE_ERROR_" + operation;
        incrementErrorCounter(errorKey);
        
        log.error("文件操作失败 - 操作: {}, 文件名: {}, 错误信息: {}", operation, fileName, error.getMessage());
        
        if (operation.contains("upload")) {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_ATTACHMENT_UPLOAD_FAILED);
        } else if (operation.contains("download")) {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_ATTACHMENT_DOWNLOAD_FAILED);
        } else if (operation.contains("delete")) {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_ATTACHMENT_DELETE_FAILED);
        } else {
            return ServiceExceptionUtil.exception(HelpOrderErrorCodes.FILE_SIZE_EXCEEDED);
        }
    }

    /**
     * 获取错误统计信息
     *
     * @return 错误统计Map
     */
    public Map<String, Integer> getErrorStatistics() {
        Map<String, Integer> statistics = new ConcurrentHashMap<>();
        errorCounters.forEach((key, counter) -> statistics.put(key, counter.get()));
        return statistics;
    }

    /**
     * 获取错误恢复建议
     *
     * @param errorType 错误类型
     * @return 恢复建议
     */
    public String getErrorRecoverySuggestion(String errorType) {
        switch (errorType) {
            case "CREATE_ERROR":
                return "建议检查工单标题、描述是否为空，意向药品和附件数量是否超出限制";
            case "UPDATE_ERROR":
                return "建议确认工单是否存在，检查更新权限";
            case "STATUS_TRANSITION_ERROR":
                return "建议检查工单当前状态是否允许转换到目标状态";
            case "ASSIGN_ERROR":
                return "建议确认分配的人员是否存在，检查是否已经分配过";
            case "QUERY_ERROR":
                return "建议检查查询参数是否正确，确认分页参数范围";
            case "FILE_ERROR":
                return "建议检查文件大小、格式是否符合要求，确认文件路径是否正确";
            default:
                return "建议联系系统管理员或查看详细日志";
        }
    }

    /**
     * 重置错误统计
     */
    public void resetErrorStatistics() {
        errorCounters.clear();
        log.info("错误统计已重置");
    }

    /**
     * 增加错误计数器
     *
     * @param errorKey 错误键
     */
    private void incrementErrorCounter(String errorKey) {
        errorCounters.computeIfAbsent(errorKey, k -> new AtomicInteger(0)).incrementAndGet();
    }

    /**
     * 记录错误详情到监控系统
     *
     * @param errorType 错误类型
     * @param errorMessage 错误信息
     * @param context 上下文信息
     */
    public void recordErrorToMonitoring(String errorType, String errorMessage, Map<String, Object> context) {
        log.error("监控记录 - 错误类型: {}, 错误信息: {}, 上下文: {}", 
                errorType, errorMessage, context);
        
        // 这里可以集成到实际的监控系统，如Prometheus、Micrometer等
        incrementErrorCounter("MONITORING_" + errorType);
    }
}
