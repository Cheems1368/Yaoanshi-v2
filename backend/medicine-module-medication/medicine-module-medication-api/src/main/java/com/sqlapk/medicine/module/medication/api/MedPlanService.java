package com.sqlapk.medicine.module.medication.api;

import com.sqlapk.medicine.module.medication.api.dto.MedPlanCreateReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.MedPlanUpdateReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.MedPlanRespDTO;

import java.util.List;

/**
 * 用药计划服务 API
 *
 * @author sqlapk
 */
public interface MedPlanService {

    /**
     * 创建用药计划
     *
     * @param createReqDTO 创建请求
     * @return 计划ID
     */
    Long createMedPlan(MedPlanCreateReqDTO createReqDTO);

    /**
     * 更新用药计划
     *
     * @param updateReqDTO 更新请求
     */
    void updateMedPlan(MedPlanUpdateReqDTO updateReqDTO);

    /**
     * 获取用药计划
     *
     * @param id 计划ID
     * @return 用药计划
     */
    MedPlanRespDTO getMedPlan(Long id);

    /**
     * 根据患者ID获取用药计划列表
     *
     * @param patientId 患者ID
     * @return 用药计划列表
     */
    List<MedPlanRespDTO> getMedPlansByPatientId(Long patientId);

    /**
     * 暂停用药计划
     *
     * @param id 计划ID
     */
    void pauseMedPlan(Long id);

    /**
     * 恢复用药计划
     *
     * @param id 计划ID
     */
    void resumeMedPlan(Long id);

    /**
     * 终止用药计划
     *
     * @param id 计划ID
     */
    void terminateMedPlan(Long id);

}
