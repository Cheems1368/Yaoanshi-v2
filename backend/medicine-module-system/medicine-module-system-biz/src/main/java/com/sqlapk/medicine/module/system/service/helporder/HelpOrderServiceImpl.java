package com.sqlapk.medicine.module.system.service.helporder;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.security.core.util.SecurityFrameworkUtils;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderCreateReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderPageReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderRespDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderUpdateReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.TimelineEventDTO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderExportReqVO;
import com.sqlapk.medicine.module.system.convert.helporder.HelpOrderConvert;
import com.sqlapk.medicine.module.system.dal.dataobject.helporder.HelpOrderDO;
import com.sqlapk.medicine.module.system.dal.mysql.helporder.HelpOrderMapper;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import com.sqlapk.medicine.module.system.monitoring.HelpOrderMetricsCollector;
import com.sqlapk.medicine.module.system.monitoring.HelpOrderPerformanceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.sqlapk.medicine.module.system.enums.HelpOrderErrorCodes.*;

/**
 * 工单 Service 实现类
 *
 * @author sqlapk
 */
@Service
@Validated
@Slf4j
public class HelpOrderServiceImpl implements HelpOrderService {

    @Resource
    private HelpOrderMapper helpOrderMapper;
    
    @Resource
    private HelpOrderValidationUtils validationUtils;
    
    @Resource
    private HelpOrderPerformanceOptimizer performanceOptimizer;
    
    @Resource
    private HelpOrderMetricsCollector metricsCollector;
    
    @Resource
    private HelpOrderErrorHandler errorHandler;
    
    @Resource
    private HelpOrderPerformanceMonitor performanceMonitor;

    @Override
    public Long createHelpOrder(HelpOrderCreateReqDTO createReqDTO) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 插入
            HelpOrderDO helpOrder = HelpOrderConvert.INSTANCE.convert(createReqDTO);
            helpOrder.setStatus(HelpOrderStatus.SUBMITTED);
            
            // 处理JSON字段
            if (createReqDTO.getIntendedDrugs() != null) {
                helpOrder.setIntendedDrugs(JSONUtil.toJsonStr(createReqDTO.getIntendedDrugs()));
            }
            if (createReqDTO.getAttachments() != null) {
                helpOrder.setAttachments(JSONUtil.toJsonStr(createReqDTO.getAttachments()));
            }
            
            helpOrderMapper.insert(helpOrder);
            
            // 记录监控指标
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordOrderCreated(createReqDTO.getPatientId(), HelpOrderStatus.SUBMITTED);
            metricsCollector.recordOperation("create_help_order", duration);
            
            // 记录性能监控
            performanceMonitor.recordApiCall("createHelpOrder", duration, true);
            performanceMonitor.recordOrderCreated(createReqDTO.getPatientId(), HelpOrderStatus.SUBMITTED.getCode());
            
            return helpOrder.getId();
            
        } catch (Exception e) {
            // 记录错误和性能监控
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordError("CREATE_ORDER_ERROR", e.getMessage(), createReqDTO.getPatientId());
            performanceMonitor.recordApiCall("createHelpOrder", duration, false);
            
            // 使用增强的错误处理
            throw errorHandler.handleCreateError(createReqDTO.getPatientId(), e);
        }
    }

    @Override
    public void updateHelpOrder(HelpOrderUpdateReqDTO updateReqDTO) {
        // 校验存在
        validationUtils.validateHelpOrderExistsAndGet(updateReqDTO.getId());
        // 更新
        HelpOrderDO updateObj = HelpOrderConvert.INSTANCE.convert(updateReqDTO);
        
        // 处理JSON字段
        if (updateReqDTO.getIntendedDrugs() != null) {
            updateObj.setIntendedDrugs(JSONUtil.toJsonStr(updateReqDTO.getIntendedDrugs()));
        }
        if (updateReqDTO.getAttachments() != null) {
            updateObj.setAttachments(JSONUtil.toJsonStr(updateReqDTO.getAttachments()));
        }
        
        helpOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteHelpOrder(Long id) {
        // 校验存在
        validationUtils.validateHelpOrderExistsAndGet(id);
        // 删除
        helpOrderMapper.deleteById(id);
    }

    @Override
    public HelpOrderRespDTO getHelpOrder(Long id) {
        HelpOrderDO helpOrder = validationUtils.validateHelpOrderExistsAndGet(id);
        return convertToRespDTO(helpOrder);
    }

    @Override
    public List<HelpOrderRespDTO> getHelpOrderList(List<Long> ids) {
        try {
            return performanceOptimizer.monitorQueryPerformance("批量查询工单", () -> {
                List<HelpOrderDO> list = performanceOptimizer.getBatchHelpOrdersOptimized(ids);
                return HelpOrderConvert.INSTANCE.convertList(list);
            });
        } catch (Exception e) {
            throw new RuntimeException("批量查询工单失败", e);
        }
    }

    @Override
    public PageResult<HelpOrderRespDTO> getHelpOrderPage(HelpOrderPageReqDTO pageReqDTO) {
        // 使用性能监控
        try {
            return performanceOptimizer.monitorQueryPerformance("分页查询工单", () -> {
                // 验证分页参数
                validationUtils.validatePageParams(pageReqDTO.getPageNo(), pageReqDTO.getPageSize());
                
                // 使用优化的查询构建器
                LambdaQueryWrapper<HelpOrderDO> queryWrapper = performanceOptimizer.buildOptimizedPageQuery(
                    pageReqDTO.getStatus() != null ? pageReqDTO.getStatus().getCode() : null,
                    pageReqDTO.getPatientId(),
                    pageReqDTO.getAssignedPharmacistId(),
                    pageReqDTO.getAssignedDoctorId(),
                    pageReqDTO.getTitle(),
                    pageReqDTO.getCreateTime()
                );
                
                PageResult<HelpOrderDO> pageResult = helpOrderMapper.selectPage(pageReqDTO, queryWrapper);
                return HelpOrderConvert.INSTANCE.convertPage(pageResult);
            });
        } catch (Exception e) {
            throw new RuntimeException("分页查询工单失败", e);
        }
    }

    @Override
    public HelpOrderRespDTO assignToPharmacist(Long id, Long pharmacistId) {
        HelpOrderDO helpOrder = validationUtils.validateHelpOrderExistsAndGet(id);
        
        // 校验状态
        if (!helpOrder.getStatus().canAssignPharmacist()) {
            throw ServiceExceptionUtil.exception(HELP_ORDER_STATUS_INVALID);
        }
        
        // 更新分配药师
        helpOrder.setAssignedPharmacistId(pharmacistId);
        helpOrder.setAssignedAt(LocalDateTime.now());
        
        // 状态转换
        if (helpOrder.getStatus() == HelpOrderStatus.SUBMITTED) {
            helpOrder.setStatus(HelpOrderStatus.ASSIGNED_PHARMACIST);
        } else if (helpOrder.getStatus() == HelpOrderStatus.ASSIGNED_PHARMACIST) {
            helpOrder.setStatus(HelpOrderStatus.PHARMACIST_REVIEW);
        }
        
        helpOrderMapper.updateById(helpOrder);
        return convertToRespDTO(helpOrder);
    }

    @Override
    public HelpOrderRespDTO escalateToDoctor(Long id, Boolean needDoctor, Long doctorId) {
        HelpOrderDO helpOrder = validationUtils.validateHelpOrderExistsAndGet(id);
        
        // 校验状态
        if (!helpOrder.getStatus().canEscalateToDoctor()) {
            throw ServiceExceptionUtil.exception(HELP_ORDER_STATUS_INVALID);
        }
        
        if (needDoctor && doctorId != null) {
            // 更新分配医生
            helpOrder.setAssignedDoctorId(doctorId);
            helpOrder.setEscalatedAt(LocalDateTime.now());
            helpOrder.setStatus(HelpOrderStatus.TO_DOCTOR);
        } else {
            // 如果不需要医生，保持当前状态或设置为建议就绪
            helpOrder.setStatus(HelpOrderStatus.ADVICE_READY);
        }
        
        helpOrderMapper.updateById(helpOrder);
        return convertToRespDTO(helpOrder);
    }

    @Override
    public HelpOrderRespDTO rejectHelpOrder(Long id, String reason) {
        HelpOrderDO helpOrder = validationUtils.validateHelpOrderExistsAndGet(id);
        
        // 校验状态
        if (!helpOrder.getStatus().canReject()) {
            throw ServiceExceptionUtil.exception(HELP_ORDER_STATUS_INVALID);
        }
        
        // 更新驳回信息
        helpOrder.setRejectedAt(LocalDateTime.now());
        helpOrder.setRejectedBy(SecurityFrameworkUtils.getLoginUserId());
        helpOrder.setRejectReason(reason);
        helpOrder.setStatus(HelpOrderStatus.REJECTED);
        
        helpOrderMapper.updateById(helpOrder);
        
        // 记录状态变更时间线
        addTimelineEvent(id, "REJECTED", 
            String.format("工单被驳回，原因：%s", reason), 
            reason);
        
        return convertToRespDTO(helpOrder);
    }



    /**
     * 转换为响应DTO，包含时间线
     */
    private HelpOrderRespDTO convertToRespDTO(HelpOrderDO helpOrder) {
        HelpOrderRespDTO respDTO = HelpOrderConvert.INSTANCE.convert(helpOrder);
        
        // 处理JSON字段
        if (StrUtil.isNotBlank(helpOrder.getIntendedDrugs())) {
            respDTO.setIntendedDrugs(JSONUtil.toList(helpOrder.getIntendedDrugs(), String.class));
        }
        if (StrUtil.isNotBlank(helpOrder.getAttachments())) {
            respDTO.setAttachments(JSONUtil.toList(helpOrder.getAttachments(), String.class));
        }
        
        // 生成时间线
        respDTO.setTimeline(generateTimeline(helpOrder));
        
        return respDTO;
    }

    /**
     * 生成时间线
     */
    private List<TimelineEventDTO> generateTimeline(HelpOrderDO helpOrder) {
        List<TimelineEventDTO> timeline = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        
        // 1. 提交时间线
        if (helpOrder.getCreateTime() != null) {
            TimelineEventDTO event = new TimelineEventDTO();
            event.setAt(helpOrder.getCreateTime().format(formatter));
            event.setEvent("SUBMITTED");
            event.setBy(helpOrder.getPatientId());
            timeline.add(event);
        }
        
        // 2. 药师认领时间线
        if (helpOrder.getAssignedPharmacistId() != null) {
            TimelineEventDTO event = new TimelineEventDTO();
            LocalDateTime at = helpOrder.getAssignedAt() != null ? 
                helpOrder.getAssignedAt() : helpOrder.getUpdateTime() != null ? 
                helpOrder.getUpdateTime() : helpOrder.getCreateTime();
            event.setAt(at.format(formatter));
            event.setEvent("ASSIGNED_PHARMACIST");
            event.setBy(helpOrder.getAssignedPharmacistId());
            timeline.add(event);
        }
        
        // 3. 升级医生时间线
        if (helpOrder.getAssignedDoctorId() != null || 
            helpOrder.getStatus() == HelpOrderStatus.TO_DOCTOR || 
            helpOrder.getStatus() == HelpOrderStatus.DOCTOR_REVIEW) {
            TimelineEventDTO event = new TimelineEventDTO();
            LocalDateTime at = helpOrder.getEscalatedAt() != null ? 
                helpOrder.getEscalatedAt() : helpOrder.getUpdateTime() != null ? 
                helpOrder.getUpdateTime() : helpOrder.getCreateTime();
            event.setAt(at.format(formatter));
            event.setEvent(helpOrder.getStatus() == HelpOrderStatus.DOCTOR_REVIEW ? 
                "DOCTOR_REVIEW" : "TO_DOCTOR");
            event.setBy(helpOrder.getAssignedPharmacistId());
            event.setTo(helpOrder.getAssignedDoctorId());
            timeline.add(event);
        }
        
        // 4. 医生处理完成时间线
        if (helpOrder.getStatus() == HelpOrderStatus.PRESCRIPTION_READY || 
            helpOrder.getStatus() == HelpOrderStatus.DONE) {
            TimelineEventDTO event = new TimelineEventDTO();
            LocalDateTime at = helpOrder.getPrescriptionReadyAt() != null ? 
                helpOrder.getPrescriptionReadyAt() : 
                helpOrder.getDoneAt() != null ? helpOrder.getDoneAt() : 
                helpOrder.getUpdateTime() != null ? helpOrder.getUpdateTime() : 
                helpOrder.getCreateTime();
            event.setAt(at.format(formatter));
            event.setEvent(helpOrder.getStatus() == HelpOrderStatus.DONE ? 
                "DONE" : "PRESCRIPTION_READY");
            event.setBy(helpOrder.getAssignedDoctorId());
            timeline.add(event);
        }
        
        // 5. 驳回时间线
        if (helpOrder.getStatus() == HelpOrderStatus.REJECTED) {
            TimelineEventDTO event = new TimelineEventDTO();
            LocalDateTime at = helpOrder.getRejectedAt() != null ? 
                helpOrder.getRejectedAt() : helpOrder.getUpdateTime() != null ? 
                helpOrder.getUpdateTime() : helpOrder.getCreateTime();
            event.setAt(at.format(formatter));
            event.setEvent("REJECTED");
            event.setBy(helpOrder.getRejectedBy());
            timeline.add(event);
        }
        
        return timeline;
    }

    /**
     * 校验状态转换是否有效
     */
    private boolean isValidStatusTransition(HelpOrderStatus currentStatus, HelpOrderStatus targetStatus) {
        // 定义有效的状态转换规则
        switch (currentStatus) {
            case SUBMITTED:
                return targetStatus == HelpOrderStatus.ASSIGNED_PHARMACIST || 
                       targetStatus == HelpOrderStatus.REJECTED;
            case ASSIGNED_PHARMACIST:
                return targetStatus == HelpOrderStatus.PHARMACIST_REVIEW || 
                       targetStatus == HelpOrderStatus.REJECTED;
            case PHARMACIST_REVIEW:
                return targetStatus == HelpOrderStatus.ADVICE_READY || 
                       targetStatus == HelpOrderStatus.TO_DOCTOR || 
                       targetStatus == HelpOrderStatus.REJECTED;
            case ADVICE_READY:
                return targetStatus == HelpOrderStatus.DONE || 
                       targetStatus == HelpOrderStatus.TO_DOCTOR || 
                       targetStatus == HelpOrderStatus.REJECTED;
            case TO_DOCTOR:
                return targetStatus == HelpOrderStatus.DOCTOR_REVIEW || 
                       targetStatus == HelpOrderStatus.REJECTED;
            case DOCTOR_REVIEW:
                return targetStatus == HelpOrderStatus.PRESCRIPTION_READY || 
                       targetStatus == HelpOrderStatus.REJECTED;
            case PRESCRIPTION_READY:
                return targetStatus == HelpOrderStatus.DONE || 
                       targetStatus == HelpOrderStatus.REJECTED;
            case DONE:
            case REJECTED:
                return false; // 终态不能转换
            default:
                return false;
        }
    }

    /**
     * 更新状态对应的时间戳
     */
    private void updateStatusTimestamp(HelpOrderDO helpOrder, HelpOrderStatus status) {
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case ASSIGNED_PHARMACIST:
                helpOrder.setAssignedAt(now);
                break;
            case TO_DOCTOR:
                helpOrder.setEscalatedAt(now);
                break;
            case PRESCRIPTION_READY:
                helpOrder.setPrescriptionReadyAt(now);
                break;
            case DONE:
                helpOrder.setDoneAt(now);
                break;
            case REJECTED:
                helpOrder.setRejectedAt(now);
                helpOrder.setRejectedBy(SecurityFrameworkUtils.getLoginUserId());
                break;
            default:
                // 其他状态不需要特殊时间戳
                break;
        }
    }

    @Override
    public HelpOrderRespDTO updateHelpOrderStatus(Long id, HelpOrderStatus status, String reason) {
        return updateHelpOrderStatusInternal(id, status, reason);
    }

    @Override
    public HelpOrderRespDTO updateHelpOrderStatus(Long id, String statusStr, String reason) {
        HelpOrderStatus status = parseStatus(statusStr);
        if (status == null) {
            throw ServiceExceptionUtil.exception(HELP_ORDER_STATUS_PARSE_ERROR);
        }
        return updateHelpOrderStatusInternal(id, status, reason);
    }

    /**
     * 内部状态更新方法
     */
    private HelpOrderRespDTO updateHelpOrderStatusInternal(Long id, HelpOrderStatus status, String reason) {
        // 1. 校验工单是否存在
        HelpOrderDO helpOrder = validationUtils.validateHelpOrderExistsAndGet(id);

        // 2. 校验状态转换是否有效
        validationUtils.validateStatusTransitionOrThrow(helpOrder.getStatus(), status, id);

        // 3. 更新工单状态
        HelpOrderDO updateObj = new HelpOrderDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        updateObj.setUpdateTime(LocalDateTime.now());
        helpOrderMapper.updateById(updateObj);

        // 4. 记录状态变更时间线
        addTimelineEvent(id, "STATUS_CHANGE", 
            String.format("状态从 %s 变更为 %s", 
                helpOrder.getStatus().getCode(), status.getCode()), 
            reason);

        // 5. 返回更新后的工单信息
        return getHelpOrder(id);
    }


    @Override
    public List<HelpOrderDO> getHelpOrderListForExport(HelpOrderExportReqVO exportReqVO) {
        try {
            return performanceOptimizer.monitorQueryPerformance("导出工单查询", () -> {
                // 使用优化的查询构建器
                LambdaQueryWrapper<HelpOrderDO> queryWrapper = performanceOptimizer.buildOptimizedPageQuery(
                    exportReqVO.getStatus(),
                    exportReqVO.getPatientId(),
                    exportReqVO.getAssignedPharmacistId(),
                    exportReqVO.getAssignedDoctorId(),
                    exportReqVO.getTitle(),
                    exportReqVO.getCreateTime()
                );
                
                return helpOrderMapper.selectList(queryWrapper);
            });
        } catch (Exception e) {
            throw new RuntimeException("导出工单查询失败", e);
        }
    }

    /**
     * 解析状态字符串
     */
    private HelpOrderStatus parseStatus(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return null;
        }
        try {
            return HelpOrderStatus.fromCode(statusStr);
        } catch (IllegalArgumentException e) {
            try {
                int ordinal = Integer.parseInt(statusStr);
                if (ordinal >= 0 && ordinal < HelpOrderStatus.values().length) {
                    return HelpOrderStatus.values()[ordinal];
                }
            } catch (NumberFormatException ex) {
                // 忽略解析错误
            }
        }
        return null;
    }

    /**
     * 添加时间线事件
     */
    private void addTimelineEvent(Long orderId, String eventType, String description, String reason) {
        // 这里可以添加时间线事件的实现
        // 目前只是记录日志
        log.info("时间线事件 - 工单ID: {}, 事件类型: {}, 描述: {}, 原因: {}", 
                orderId, eventType, description, reason);
    }
}

