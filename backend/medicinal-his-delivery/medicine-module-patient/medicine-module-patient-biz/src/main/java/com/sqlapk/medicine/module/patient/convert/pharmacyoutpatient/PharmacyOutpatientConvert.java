package com.sqlapk.medicine.module.patient.convert.pharmacyoutpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.pharmacyoutpatient.PharmacyOutpatientDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 药学门诊 Convert
 *
 * @author 管理员
 */
@Mapper
public interface PharmacyOutpatientConvert {

    PharmacyOutpatientConvert INSTANCE = Mappers.getMapper(PharmacyOutpatientConvert.class);

    PharmacyOutpatientDO convert(PharmacyOutpatientCreateReqVO bean);

    PharmacyOutpatientDO convert(PharmacyOutpatientUpdateReqVO bean);

    PharmacyOutpatientRespVO convert(PharmacyOutpatientDO bean);

    List<PharmacyOutpatientRespVO> convertList(List<PharmacyOutpatientDO> list);

    PageResult<PharmacyOutpatientRespVO> convertPage(PageResult<PharmacyOutpatientDO> page);

    List<PharmacyOutpatientExcelVO> convertList02(List<PharmacyOutpatientDO> list);

}
