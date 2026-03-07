package com.sqlapk.medicine.module.patient.convert.homeeducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeeducation.HomeEducationDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 居家用药教育 Convert
 *
 * @author 管理员
 */
@Mapper
public interface HomeEducationConvert {

    HomeEducationConvert INSTANCE = Mappers.getMapper(HomeEducationConvert.class);

    HomeEducationDO convert(HomeEducationCreateReqVO bean);

    HomeEducationDO convert(HomeEducationUpdateReqVO bean);

    HomeEducationRespVO convert(HomeEducationDO bean);

    List<HomeEducationRespVO> convertList(List<HomeEducationDO> list);

    PageResult<HomeEducationRespVO> convertPage(PageResult<HomeEducationDO> page);

    List<HomeEducationExcelVO> convertList02(List<HomeEducationDO> list);

}
