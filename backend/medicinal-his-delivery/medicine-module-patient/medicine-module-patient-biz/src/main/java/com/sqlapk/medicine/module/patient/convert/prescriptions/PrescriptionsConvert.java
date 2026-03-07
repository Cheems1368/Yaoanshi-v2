package com.sqlapk.medicine.module.patient.convert.prescriptions;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptions.PrescriptionsDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 处方 Convert
 *
 * @author 管理员
 */
@Mapper
public interface PrescriptionsConvert {

    PrescriptionsConvert INSTANCE = Mappers.getMapper(PrescriptionsConvert.class);

    PrescriptionsDO convert(PrescriptionsCreateReqVO bean);

    PrescriptionsDO convert(PrescriptionsUpdateReqVO bean);

    PrescriptionsRespVO convert(PrescriptionsDO bean);

    List<PrescriptionsRespVO> convertList(List<PrescriptionsDO> list);

    PageResult<PrescriptionsRespVO> convertPage(PageResult<PrescriptionsDO> page);

    List<PrescriptionsExcelVO> convertList02(List<PrescriptionsDO> list);

}
