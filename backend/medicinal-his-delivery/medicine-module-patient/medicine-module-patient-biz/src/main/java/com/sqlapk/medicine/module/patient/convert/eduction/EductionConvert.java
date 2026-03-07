package com.sqlapk.medicine.module.patient.convert.eduction;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.education.EducationDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用药教育 Convert
 *
 * @author 管理员
 */
@Mapper
public interface EductionConvert {

    EductionConvert INSTANCE = Mappers.getMapper(EductionConvert.class);

    EducationDO convert(EducationCreateReqVO bean);

    EducationDO convert(EducationUpdateReqVO bean);

    EducationRespVO convert(EducationDO bean);

    List<EducationRespVO> convertList(List<EducationDO> list);

    PageResult<EducationRespVO> convertPage(PageResult<EducationDO> page);

    List<EducationExcelVO> convertList02(List<EducationDO> list);

}
