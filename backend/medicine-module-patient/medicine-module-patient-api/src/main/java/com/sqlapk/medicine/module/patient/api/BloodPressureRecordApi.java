package com.sqlapk.medicine.module.patient.api;

import com.sqlapk.medicine.module.patient.api.dto.health.BloodPressureRecordReqDTO;
import com.sqlapk.medicine.module.patient.api.dto.health.BloodPressureRecordRespDTO;
import com.sqlapk.medicine.module.patient.api.dto.health.BloodPressureTrendRespDTO;

import java.util.List;

/**
 * 血压记录 API 接口
 *
 * @author sqlapk
 */
public interface BloodPressureRecordApi {

    /**
     * 创建血压记录
     *
     * @param reqDTO 血压记录请求
     * @return 血压记录ID
     */
    Long createBloodPressureRecord(BloodPressureRecordReqDTO reqDTO);

    /**
     * 获取患者最近的血压记录
     *
     * @param patientId 患者ID
     * @param limit 记录数量限制
     * @return 血压记录列表
     */
    List<BloodPressureRecordRespDTO> getRecentBloodPressureRecords(Long patientId, Integer limit);

    /**
     * 获取患者血压趋势分析
     *
     * @param patientId 患者ID
     * @param days 统计天数
     * @return 血压趋势数据
     */
    BloodPressureTrendRespDTO getBloodPressureTrend(Long patientId, Integer days);

    /**
     * 获取患者血压统计信息
     *
     * @param patientId 患者ID
     * @param days 统计天数
     * @return 血压统计信息
     */
    BloodPressureTrendRespDTO getBloodPressureStats(Long patientId, Integer days);

    /**
     * 批量创建血压记录
     *
     * @param reqDTOList 血压记录请求列表
     * @return 创建成功的记录ID列表
     */
    List<Long> batchCreateBloodPressureRecords(List<BloodPressureRecordReqDTO> reqDTOList);
}
