package com.sqlapk.medicine.module.medication.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqlapk.medicine.framework.common.exception.ServiceException;
import com.sqlapk.medicine.module.medication.api.MedPlanService;
import com.sqlapk.medicine.module.medication.api.TodayTaskService;
import com.sqlapk.medicine.module.medication.api.dto.*;
import com.sqlapk.medicine.module.medication.convert.MedPlanConvert;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedPlanDO;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedPlanItemDO;
import com.sqlapk.medicine.module.medication.dal.mysql.MedPlanItemMapper;
import com.sqlapk.medicine.module.medication.dal.mysql.MedPlanMapper;
import com.sqlapk.medicine.module.medication.enums.ErrorCodeConstants;
import com.sqlapk.medicine.module.medication.enums.MedPlanStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用药计划服务实现
 *
 * @author sqlapk
 */
@Service
@Slf4j
public class MedPlanServiceImpl implements MedPlanService {

    @Autowired
    private MedPlanMapper medPlanMapper;

    @Autowired
    private MedPlanItemMapper medPlanItemMapper;

    @Autowired
    private TodayTaskService todayTaskService;

    @Override
    @Transactional
    public Long createMedPlan(MedPlanCreateReqDTO createReqDTO) {
        log.info("创建用药计划，患者ID: {}, 计划名称: {}", createReqDTO.getPatientId(), createReqDTO.getPlanName());

        // 1. 创建用药计划
        MedPlanDO medPlan = MedPlanConvert.INSTANCE.convert(createReqDTO);
        medPlan.setStatus(MedPlanStatus.ACTIVE.getStatus());
        medPlan.setVersion(1);
        medPlanMapper.insert(medPlan);

        // 2. 创建用药计划项
        for (MedPlanItemCreateReqDTO itemReqDTO : createReqDTO.getItems()) {
            MedPlanItemDO planItem = MedPlanConvert.INSTANCE.convert(itemReqDTO);
            planItem.setPlanId(medPlan.getId());
            medPlanItemMapper.insert(planItem);
        }

        // 3. 生成今日待办任务
        todayTaskService.generateTodayTasks(medPlan.getId());

        log.info("用药计划创建成功，计划ID: {}", medPlan.getId());
        return medPlan.getId();
    }

    @Override
    @Transactional
    public void updateMedPlan(MedPlanUpdateReqDTO updateReqDTO) {
        log.info("更新用药计划，计划ID: {}", updateReqDTO.getId());

        // 1. 校验计划存在
        MedPlanDO existingPlan = medPlanMapper.selectById(updateReqDTO.getId());
        if (existingPlan == null) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_NOT_EXISTS);
        }

        // 2. 更新计划信息
        MedPlanDO medPlan = MedPlanConvert.INSTANCE.convert(updateReqDTO);
        medPlan.setVersion(existingPlan.getVersion() + 1);
        medPlanMapper.updateById(medPlan);

        // 3. 更新计划项
        updateMedPlanItems(medPlan.getId(), updateReqDTO.getItems());

        // 4. 重新生成今日待办任务
        todayTaskService.regenerateTodayTasks(medPlan.getId());

        log.info("用药计划更新成功，计划ID: {}", medPlan.getId());
    }

    @Override
    public MedPlanRespDTO getMedPlan(Long id) {
        log.info("获取用药计划，计划ID: {}", id);

        MedPlanDO medPlan = medPlanMapper.selectById(id);
        if (medPlan == null) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_NOT_EXISTS);
        }

        MedPlanRespDTO respDTO = MedPlanConvert.INSTANCE.convert(medPlan);

        // 获取计划项
        List<MedPlanItemDO> planItems = medPlanItemMapper.selectByPlanId(id);
        List<MedPlanItemRespDTO> itemRespDTOs = MedPlanConvert.INSTANCE.convertItems(planItems);
        respDTO.setItems(itemRespDTOs);

        return respDTO;
    }

    @Override
    public List<MedPlanRespDTO> getMedPlansByPatientId(Long patientId) {
        log.info("根据患者ID获取用药计划列表，患者ID: {}", patientId);

        List<MedPlanDO> medPlans = medPlanMapper.selectByPatientId(patientId);
        List<MedPlanRespDTO> respDTOs = MedPlanConvert.INSTANCE.convertList(medPlans);

        // 为每个计划填充计划项
        for (MedPlanRespDTO respDTO : respDTOs) {
            List<MedPlanItemDO> planItems = medPlanItemMapper.selectByPlanId(respDTO.getId());
            List<MedPlanItemRespDTO> itemRespDTOs = MedPlanConvert.INSTANCE.convertItems(planItems);
            respDTO.setItems(itemRespDTOs);
        }

        return respDTOs;
    }

    @Override
    @Transactional
    public void pauseMedPlan(Long id) {
        log.info("暂停用药计划，计划ID: {}", id);

        MedPlanDO medPlan = medPlanMapper.selectById(id);
        if (medPlan == null) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_NOT_EXISTS);
        }

        medPlan.setStatus(MedPlanStatus.PAUSED.getStatus());
        medPlanMapper.updateById(medPlan);

        // 暂停相关任务
        todayTaskService.pauseTasksByPlanId(id);

        log.info("用药计划暂停成功，计划ID: {}", id);
    }

    @Override
    @Transactional
    public void resumeMedPlan(Long id) {
        log.info("恢复用药计划，计划ID: {}", id);

        MedPlanDO medPlan = medPlanMapper.selectById(id);
        if (medPlan == null) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_NOT_EXISTS);
        }

        medPlan.setStatus(MedPlanStatus.ACTIVE.getStatus());
        medPlanMapper.updateById(medPlan);

        // 恢复相关任务
        todayTaskService.resumeTasksByPlanId(id);

        log.info("用药计划恢复成功，计划ID: {}", id);
    }

    @Override
    @Transactional
    public void terminateMedPlan(Long id) {
        log.info("终止用药计划，计划ID: {}", id);

        MedPlanDO medPlan = medPlanMapper.selectById(id);
        if (medPlan == null) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_NOT_EXISTS);
        }

        medPlan.setStatus(MedPlanStatus.TERMINATED.getStatus());
        medPlanMapper.updateById(medPlan);

        // 终止相关任务
        todayTaskService.terminateTasksByPlanId(id);

        log.info("用药计划终止成功，计划ID: {}", id);
    }

    /**
     * 更新计划项
     */
    private void updateMedPlanItems(Long planId, List<MedPlanItemUpdateReqDTO> items) {
        // 删除原有计划项
        medPlanItemMapper.deleteByPlanId(planId);

        // 创建新计划项
        for (MedPlanItemUpdateReqDTO itemReqDTO : items) {
            MedPlanItemDO planItem = MedPlanConvert.INSTANCE.convert(itemReqDTO);
            planItem.setPlanId(planId);
            medPlanItemMapper.insert(planItem);
        }
    }

}
