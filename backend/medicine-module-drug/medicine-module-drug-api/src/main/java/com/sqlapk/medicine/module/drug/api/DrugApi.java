package com.sqlapk.medicine.module.drug.api;

import com.sqlapk.medicine.module.drug.api.dto.DrugRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 药品 API 接口
 *
 * @author sqlapk
 */
public interface DrugApi {

    /**
     * 获得药品信息
     *
     * @param id 药品编号
     * @return 药品信息
     */
    DrugRespDTO getDrug(Long id);

    /**
     * 获得药品信息列表
     *
     * @param ids 药品编号列表
     * @return 药品信息列表
     */
    List<DrugRespDTO> getDrugList(Collection<Long> ids);

    /**
     * 根据药品名称获得药品信息
     *
     * @param drugName 药品名称
     * @return 药品信息
     */
    DrugRespDTO getDrugByName(String drugName);

}
