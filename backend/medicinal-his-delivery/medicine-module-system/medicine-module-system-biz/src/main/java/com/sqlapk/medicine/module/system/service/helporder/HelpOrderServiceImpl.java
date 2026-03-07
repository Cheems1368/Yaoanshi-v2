package com.sqlapk.medicine.module.system.service.helporder;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.security.core.util.SecurityFrameworkUtils;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderCreateReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderPageReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderRespDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderUpdateReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.TimelineEventDTO;
import com.sqlapk.medicine.module.system.convert.helporder.HelpOrderConvert;
import com.sqlapk.medicine.module.system.dal.dataobject.helporder.HelpOrderDO;
import com.sqlapk.medicine.module.system.dal.mysql.helporder.HelpOrderMapper;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.sqlapk.medicine.module.system.enums.ErrorCodeConstants.*;

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

    @Override
    public Long createHelpOrder(HelpOrderCreateReqDTO createReqDTO) {
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
        // 返回
        return helpOrder.getId();
    }

    @Override
    public void updateHelpOrder(HelpOrderUpdateReqDTO updateReqDTO) {
        // 校验存在
        validateHelpOrderExists(updateReqDTO.getId());
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
        validateHelpOrderExists(id);
        // 删除
        helpOrderMapper.deleteById(id);
    }

    private void validateHelpOrderExists(Long id) {
        if (helpOrderMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(HELP_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public HelpOrderRespDTO getHelpOrder(Long id) {
        HelpOrderDO helpOrder = helpOrderMapper.selectById(id);
        if (helpOrder == null) {
            throw ServiceExceptionUtil.exception(HELP_ORDER_NOT_EXISTS);
        }
        return convertToRespDTO(helpOrder);
    }

    @Override
    public List<HelpOrderRespDTO> getHelpOrderList(List<Long> ids) {
        List<HelpOrderDO> list = helpOrderMapper.selectList(ids);
        return HelpOrderConvert.INSTANCE.convertList(list);
    }

    @Override
    public PageResult<HelpOrderRespDTO> getHelpOrderPage(HelpOrderPageReqDTO pageReqDTO) {
        PageResult<HelpOrderDO> pageResult = helpOrderMapper.selectPage(pageReqDTO);
        return HelpOrderConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public HelpOrderRespDTO assignToPharmacist(Long id, Long pharmacistId) {
        HelpOrderDO helpOrder = validateHelpOrderExistsAndGet(id);
        
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
    public HelpOrderRespDTO escalateToDoctor(Long id, Long doctorId) {
        HelpOrderDO helpOrder = validateHelpOrderExistsAndGet(id);
        
        // 校验状态
        if (!helpOrder.getStatus().canEscalateToDoctor()) {
            throw ServiceExceptionUtil.exception(HELP_ORDER_STATUS_INVALID);
        }
        
        // 更新分配医生
        helpOrder.setAssignedDoctorId(doctorId);
        helpOrder.setEscalatedAt(LocalDateTime.now());
        helpOrder.setStatus(HelpOrderStatus.TO_DOCTOR);
        
        helpOrderMapper.updateById(helpOrder);
        return convertToRespDTO(helpOrder);
    }

    @Override
    public HelpOrderRespDTO rejectHelpOrder(Long id, String reason) {
        HelpOrderDO helpOrder = validateHelpOrderExistsAndGet(id);
        
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
        return convertToRespDTO(helpOrder);
    }

    private HelpOrderDO validateHelpOrderExistsAndGet(Long id) {
        HelpOrderDO helpOrder = helpOrderMapper.selectById(id);
        if (helpOrder == null) {
            throw ServiceExceptionUtil.exception(HELP_ORDER_NOT_EXISTS);
        }
        return helpOrder;
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
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
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

}

