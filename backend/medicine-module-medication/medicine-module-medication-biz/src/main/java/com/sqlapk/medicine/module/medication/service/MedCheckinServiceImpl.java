package com.sqlapk.medicine.module.medication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlapk.medicine.framework.common.exception.ServiceException;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.medication.api.MedCheckinService;
import com.sqlapk.medicine.module.medication.api.dto.*;
import com.sqlapk.medicine.module.medication.convert.MedCheckinConvert;
import com.sqlapk.medicine.module.medication.controller.admin.vo.CheckinPageReqVO;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedCheckinDO;
import com.sqlapk.medicine.module.medication.dal.dataobject.TodayTaskDO;
import com.sqlapk.medicine.module.medication.dal.mysql.MedCheckinMapper;
import com.sqlapk.medicine.module.medication.dal.mysql.TodayTaskMapper;
import com.sqlapk.medicine.module.medication.enums.CheckinStatus;
import com.sqlapk.medicine.module.medication.enums.ErrorCodeConstants;
import com.sqlapk.medicine.module.medication.enums.VerifyStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 用药打卡服务实现
 *
 * @author sqlapk
 */
@Service
@Slf4j
public class MedCheckinServiceImpl implements MedCheckinService {

    @Autowired
    private MedCheckinMapper medCheckinMapper;

    @Autowired
    private TodayTaskMapper todayTaskMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Long submitCheckin(CheckinSubmitReqDTO submitReqDTO) {
        log.info("提交打卡，待办任务ID: {}, 客户端标识: {}", submitReqDTO.getTodoId(), submitReqDTO.getClientNonce());

        // 1. 防重复提交检查
        if (isClientNonceUsed(submitReqDTO.getClientNonce())) {
            throw new ServiceException(ErrorCodeConstants.CHECKIN_ALREADY_EXISTS);
        }

        // 2. 检查待办任务是否存在
        TodayTaskDO task = todayTaskMapper.selectById(submitReqDTO.getTodoId());
        if (task == null) {
            throw new ServiceException(ErrorCodeConstants.TODAY_TASK_NOT_EXISTS);
        }

        // 3. 检查是否已有打卡记录
        MedCheckinDO existingCheckin = medCheckinMapper.selectByTodoId(submitReqDTO.getTodoId());
        if (existingCheckin != null && !CheckinStatus.REVOKED.equals(CheckinStatus.valueOf(existingCheckin.getStatus()))) {
            throw new ServiceException(ErrorCodeConstants.CHECKIN_ALREADY_EXISTS);
        }

        // 4. 创建打卡记录
        MedCheckinDO checkin = MedCheckinDO.builder()
            .todoId(submitReqDTO.getTodoId())
            .patientId(task.getPatientId())
            .checkinTime(submitReqDTO.getCheckinTime() != null ? 
                submitReqDTO.getCheckinTime() : LocalDateTime.now())
            .status(submitReqDTO.getStatus())
            .notes(submitReqDTO.getNotes())
            .evidence(convertEvidenceToJson(submitReqDTO.getEvidence()))
            .clientNonce(submitReqDTO.getClientNonce())
            .verifyStatus(VerifyStatus.PENDING.getStatus())
            .build();

        medCheckinMapper.insert(checkin);

        // 5. 更新待办任务状态
        task.setStatus(submitReqDTO.getStatus());
        task.setCompletedTime(checkin.getCheckinTime());
        task.setNotes(submitReqDTO.getNotes());
        todayTaskMapper.updateById(task);

        log.info("打卡提交成功，打卡记录ID: {}", checkin.getId());
        return checkin.getId();
    }

    @Override
    @Transactional
    public void revokeCheckin(CheckinRevokeReqDTO revokeReqDTO) {
        log.info("撤销打卡，打卡记录ID: {}", revokeReqDTO.getCheckinId());

        // 1. 检查打卡记录是否存在
        MedCheckinDO checkin = medCheckinMapper.selectById(revokeReqDTO.getCheckinId());
        if (checkin == null) {
            throw new ServiceException(ErrorCodeConstants.CHECKIN_NOT_EXISTS);
        }

        // 2. 检查状态是否允许撤销
        if (CheckinStatus.REVOKED.equals(CheckinStatus.valueOf(checkin.getStatus()))) {
            throw new ServiceException(ErrorCodeConstants.CHECKIN_STATUS_ERROR);
        }

        // 3. 更新打卡记录状态
        checkin.setStatus(CheckinStatus.REVOKED.getStatus());
        checkin.setRevokeTime(LocalDateTime.now());
        checkin.setRevokeReason(revokeReqDTO.getReason());
        medCheckinMapper.updateById(checkin);

        // 4. 恢复待办任务状态
        TodayTaskDO task = todayTaskMapper.selectById(checkin.getTodoId());
        if (task != null) {
            task.setStatus(0); // 恢复为待完成状态
            task.setCompletedTime(null);
            task.setNotes(null);
            todayTaskMapper.updateById(task);
        }

        log.info("打卡撤销成功，打卡记录ID: {}", revokeReqDTO.getCheckinId());
    }

    @Override
    @Transactional
    public void verifyCheckin(CheckinVerifyReqDTO verifyReqDTO) {
        log.info("核实打卡，打卡记录ID: {}", verifyReqDTO.getCheckinId());

        // 1. 检查打卡记录是否存在
        MedCheckinDO checkin = medCheckinMapper.selectById(verifyReqDTO.getCheckinId());
        if (checkin == null) {
            throw new ServiceException(ErrorCodeConstants.CHECKIN_NOT_EXISTS);
        }

        // 2. 更新核实状态
        checkin.setVerifyStatus(verifyReqDTO.getVerifyStatus());
        checkin.setVerifyTime(LocalDateTime.now());
        checkin.setVerifyNotes(verifyReqDTO.getVerifyNotes());
        medCheckinMapper.updateById(checkin);

        log.info("打卡核实完成，打卡记录ID: {}", verifyReqDTO.getCheckinId());
    }

    @Override
    public List<CheckinRespDTO> getCheckinsByPatientIdAndDateRange(Long patientId, LocalDate startDate, LocalDate endDate) {
        log.info("获取打卡记录，患者ID: {}, 日期范围: {} - {}", patientId, startDate, endDate);

        List<MedCheckinDO> checkins = medCheckinMapper.selectByPatientIdAndDateRange(patientId, startDate, endDate);
        List<CheckinRespDTO> respDTOs = MedCheckinConvert.INSTANCE.toDTOList(checkins);

        // 填充状态名称
        for (CheckinRespDTO respDTO : respDTOs) {
            respDTO.setStatusName(getStatusName(respDTO.getStatus()));
            respDTO.setVerifyStatusName(getVerifyStatusName(respDTO.getVerifyStatus()));
        }

        return respDTOs;
    }

    @Override
    public List<CheckinRespDTO> getCheckinsByPatientIdAndTaskDate(Long patientId, LocalDate taskDate) {
        log.info("获取打卡记录，患者ID: {}, 任务日期: {}", patientId, taskDate);

        List<MedCheckinDO> checkins = medCheckinMapper.selectByPatientIdAndTaskDate(patientId, taskDate);
        List<CheckinRespDTO> respDTOs = MedCheckinConvert.INSTANCE.toDTOList(checkins);

        // 填充状态名称
        for (CheckinRespDTO respDTO : respDTOs) {
            respDTO.setStatusName(getStatusName(respDTO.getStatus()));
            respDTO.setVerifyStatusName(getVerifyStatusName(respDTO.getVerifyStatus()));
        }

        return respDTOs;
    }

    @Override
    public CheckinRespDTO getCheckinById(Long checkinId) {
        log.info("获取打卡记录详情，打卡记录ID: {}", checkinId);

        MedCheckinDO checkin = medCheckinMapper.selectById(checkinId);
        if (checkin == null) {
            throw new ServiceException(ErrorCodeConstants.CHECKIN_NOT_EXISTS);
        }

        CheckinRespDTO respDTO = MedCheckinConvert.INSTANCE.toDTO(checkin);
        respDTO.setStatusName(getStatusName(respDTO.getStatus()));
        respDTO.setVerifyStatusName(getVerifyStatusName(respDTO.getVerifyStatus()));

        return respDTO;
    }

    @Override
    public boolean isClientNonceUsed(String clientNonce) {
        MedCheckinDO checkin = medCheckinMapper.selectByClientNonce(clientNonce);
        return checkin != null;
    }

    @Override
    public List<CheckinRespDTO> getCheckinsByPatientId(Long patientId) {
        log.info("获取患者打卡记录，患者ID: {}", patientId);

        List<MedCheckinDO> checkins = medCheckinMapper.selectByPatientId(patientId);
        List<CheckinRespDTO> respDTOs = MedCheckinConvert.INSTANCE.toDTOList(checkins);

        // 填充状态名称
        for (CheckinRespDTO respDTO : respDTOs) {
            respDTO.setStatusName(getStatusName(respDTO.getStatus()));
            respDTO.setVerifyStatusName(getVerifyStatusName(respDTO.getVerifyStatus()));
        }

        return respDTOs;
    }

    @Override
    public List<CheckinRespDTO> getRecentCheckins(Long patientId, Integer days) {
        log.info("获取最近打卡记录，患者ID: {}, 天数: {}", patientId, days);

        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<MedCheckinDO> checkins = medCheckinMapper.selectRecentCheckins(patientId, startTime);
        List<CheckinRespDTO> respDTOs = MedCheckinConvert.INSTANCE.toDTOList(checkins);

        // 填充状态名称
        for (CheckinRespDTO respDTO : respDTOs) {
            respDTO.setStatusName(getStatusName(respDTO.getStatus()));
            respDTO.setVerifyStatusName(getVerifyStatusName(respDTO.getVerifyStatus()));
        }

        return respDTOs;
    }

    @Override
    public Integer countCompletedCheckins(Long patientId, LocalDate startDate, LocalDate endDate) {
        return medCheckinMapper.countCompletedCheckins(patientId, startDate, endDate);
    }

    @Override
    public Integer countSkippedCheckins(Long patientId, LocalDate startDate, LocalDate endDate) {
        return medCheckinMapper.countSkippedCheckins(patientId, startDate, endDate);
    }

    /**
     * 转换证据信息为JSON
     */
    private String convertEvidenceToJson(CheckinSubmitReqDTO.EvidenceDTO evidence) {
        if (evidence == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(evidence);
        } catch (JsonProcessingException e) {
            log.error("转换证据信息为JSON失败", e);
            return null;
        }
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        CheckinStatus checkinStatus = CheckinStatus.valueOf(status);
        return checkinStatus != null ? checkinStatus.getName() : "未知";
    }

    /**
     * 获取核实状态名称
     */
    private String getVerifyStatusName(Integer verifyStatus) {
        VerifyStatus status = VerifyStatus.valueOf(verifyStatus);
        return status != null ? status.getName() : "未知";
    }

    @Override
    public PageResult<CheckinRespDTO> getCheckinPage(CheckinPageReqDTO pageReqDTO) {
        PageResult<MedCheckinDO> pageResult = medCheckinMapper.selectPage(pageReqDTO);
        // 将DO转换为DTO
        List<CheckinRespDTO> dtoList = pageResult.getList().stream()
                .map(MedCheckinConvert.INSTANCE::toDTO)
                .collect(java.util.stream.Collectors.toList());
        return new PageResult<>(dtoList, pageResult.getTotal());
    }

}
