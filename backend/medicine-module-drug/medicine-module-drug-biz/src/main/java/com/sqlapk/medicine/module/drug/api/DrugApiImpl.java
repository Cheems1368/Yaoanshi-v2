package com.sqlapk.medicine.module.drug.api;

import com.sqlapk.medicine.module.drug.api.dto.DrugRespDTO;
import com.sqlapk.medicine.module.drugs.convert.drug.DrugsConvert;
import com.sqlapk.medicine.module.drugs.dal.dataobject.drug.DrugsDO;
import com.sqlapk.medicine.module.drugs.service.drug.DrugsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 药品 API 实现类
 *
 * @author sqlapk
 */
@Service
public class DrugApiImpl implements DrugApi {

    @Resource
    private DrugsService drugsService;

    @Override
    public DrugRespDTO getDrug(Long id) {
        DrugsDO drug = drugsService.get(id);
        return DrugsConvert.INSTANCE.convertToApi(drug);
    }

    @Override
    public List<DrugRespDTO> getDrugList(Collection<Long> ids) {
        List<DrugsDO> drugList = drugsService.getList(ids);
        return drugList.stream()
                .map(DrugsConvert.INSTANCE::convertToApi)
                .collect(Collectors.toList());
    }

    @Override
    public DrugRespDTO getDrugByName(String drugName) {
        DrugsDO drug = drugsService.getByName(drugName);
        return DrugsConvert.INSTANCE.convertToApi(drug);
    }

}
