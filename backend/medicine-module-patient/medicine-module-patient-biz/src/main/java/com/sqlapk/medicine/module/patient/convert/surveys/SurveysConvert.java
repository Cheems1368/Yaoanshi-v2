package com.sqlapk.medicine.module.patient.convert.surveys;

import java.util.*;

import com.sqlapk.medicine.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.sqlapk.medicine.module.patient.controller.admin.surveys.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.surveys.SurveysDO;

/**
 * 问卷 Convert
 *
 * @author 管理员 */
@Mapper(componentModel = "spring")
public interface SurveysConvert {

    SurveysConvert INSTANCE = Mappers.getMapper(SurveysConvert.class);

    SurveysDO convert(SurveysCreateReqVO bean);

    SurveysDO convert(SurveysUpdateReqVO bean);

    SurveysRespVO convert(SurveysDO bean);

    List<SurveysRespVO> convertList(List<SurveysDO> list);

    PageResult<SurveysRespVO> convertPage(PageResult<SurveysDO> page);

    List<SurveysExcelVO> convertList02(List<SurveysDO> list);

}
