package com.sqlapk.medicine.module.patient.convert.inpatientconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientconsultation.InpatientConsultationDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 主要用药咨询 Convert
 *
 * @author 管理员 */
@Mapper(componentModel = "spring")
public interface InpatientConsultationConvert {

    InpatientConsultationConvert INSTANCE = Mappers.getMapper(InpatientConsultationConvert.class);

    InpatientConsultationDO convert(InpatientConsultationCreateReqVO bean);

    InpatientConsultationDO convert(InpatientConsultationUpdateReqVO bean);

    InpatientConsultationRespVO convert(InpatientConsultationDO bean);

    List<InpatientConsultationRespVO> convertList(List<InpatientConsultationDO> list);

    PageResult<InpatientConsultationRespVO> convertPage(PageResult<InpatientConsultationDO> page);

    List<InpatientConsultationExcelVO> convertList02(List<InpatientConsultationDO> list);

}
