package com.sqlapk.medicine.module.patient.convert.health;

import com.sqlapk.medicine.module.patient.api.dto.health.BloodPressureRecordReqDTO;
import com.sqlapk.medicine.module.patient.api.dto.health.BloodPressureRecordRespDTO;
import com.sqlapk.medicine.module.patient.dal.dataobject.health.BloodPressureRecordDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 血压记录转换器
 *
 * @author sqlapk
 */
@Mapper(componentModel = "spring")
public interface BloodPressureRecordConvert {

    BloodPressureRecordConvert INSTANCE = Mappers.getMapper(BloodPressureRecordConvert.class);

    /**
     * 请求DTO转DO
     */
    BloodPressureRecordDO convert(BloodPressureRecordReqDTO reqDTO);

    /**
     * DO转响应DTO
     */
    BloodPressureRecordRespDTO convert(BloodPressureRecordDO recordDO);
}
