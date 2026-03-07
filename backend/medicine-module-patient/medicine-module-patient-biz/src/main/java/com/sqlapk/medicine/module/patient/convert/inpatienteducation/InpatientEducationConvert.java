package com.sqlapk.medicine.module.patient.convert.inpatienteducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatienteducation.InpatientEducationDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 住院用药教育 Convert
 *
 * @author 管理员 */
@Mapper(componentModel = "spring")
public interface InpatientEducationConvert {

    InpatientEducationConvert INSTANCE = Mappers.getMapper(InpatientEducationConvert.class);

    InpatientEducationDO convert(InpatientEducationCreateReqVO bean);

    InpatientEducationDO convert(InpatientEducationUpdateReqVO bean);

    InpatientEducationRespVO convert(InpatientEducationDO bean);

    List<InpatientEducationRespVO> convertList(List<InpatientEducationDO> list);

    PageResult<InpatientEducationRespVO> convertPage(PageResult<InpatientEducationDO> page);

    List<InpatientEducationExcelVO> convertList02(List<InpatientEducationDO> list);

}
