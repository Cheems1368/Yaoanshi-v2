package com.sqlapk.medicine.module.patient.api;

import com.sqlapk.medicine.module.patient.api.dto.PatientsRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 患者 API 接口
 *
 * @author sqlapk
 */
public interface PatientsApi {

    /**
     * 获得患者信息
     *
     * @param id 患者编号
     * @return 患者信息
     */
    PatientsRespDTO getPatients(Long id);

    /**
     * 获得患者信息列表
     *
     * @param ids 患者编号列表
     * @return 患者信息列表
     */
    List<PatientsRespDTO> getPatientsList(Collection<Long> ids);

    /**
     * 根据用户ID获得患者信息
     *
     * @param userId 用户ID
     * @return 患者信息
     */
    PatientsRespDTO getPatientsByUserId(Long userId);

}
