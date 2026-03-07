package com.sqlapk.medicine.module.patient.convert.outpatienteducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatienteducation.OutpatientEducationDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 门诊处方用药教育明细 Convert
 *
 * @author 管理员
 */
@Mapper
public interface OutpatientEducationConvert {

    OutpatientEducationConvert INSTANCE = Mappers.getMapper(OutpatientEducationConvert.class);

    OutpatientEducationDO convert(OutpatientEducationCreateReqVO bean);

    OutpatientEducationDO convert(OutpatientEducationUpdateReqVO bean);

    OutpatientEducationRespVO convert(OutpatientEducationDO bean);

    List<OutpatientEducationRespVO> convertList(List<OutpatientEducationDO> list);

    PageResult<OutpatientEducationRespVO> convertPage(PageResult<OutpatientEducationDO> page);

    List<OutpatientEducationExcelVO> convertList02(List<OutpatientEducationDO> list);

}
