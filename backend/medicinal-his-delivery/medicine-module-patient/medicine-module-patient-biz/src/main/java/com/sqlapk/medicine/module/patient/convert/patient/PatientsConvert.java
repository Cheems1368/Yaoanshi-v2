package com.sqlapk.medicine.module.patient.convert.patient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsSimpleRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 患者 Convert
 *
 * @author 管理员
 */
@Mapper
public interface PatientsConvert {

    PatientsConvert INSTANCE = Mappers.getMapper(PatientsConvert.class);

    PatientsDO convert(PatientsCreateReqVO bean);

    PatientsDO convert(PatientsUpdateReqVO bean);

    PatientsRespVO convert(PatientsDO bean);

    List<PatientsRespVO> convertList(List<PatientsDO> list);

    PageResult<PatientsRespVO> convertPage(PageResult<PatientsDO> page);

    List<PatientsExcelVO> convertList02(List<PatientsDO> list);

    List<PatientsSimpleRespVO> convertSimple(List<PatientsDO> list);


}
