package com.sqlapk.medicine.module.patient.convert.answers;

import java.util.*;

import com.sqlapk.medicine.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.answers.AnswersDO;

/**
 * 问卷 Convert
 *
 * @author 管理员
 */
@Mapper
public interface AnswersConvert {

    AnswersConvert INSTANCE = Mappers.getMapper(AnswersConvert.class);

    AnswersDO convert(AnswersCreateReqVO bean);

    AnswersDO convert(AnswersUpdateReqVO bean);

    AnswersRespVO convert(AnswersDO bean);

    List<AnswersRespVO> convertList(List<AnswersDO> list);

    PageResult<AnswersRespVO> convertPage(PageResult<AnswersDO> page);

    List<AnswersExcelVO> convertList02(List<AnswersDO> list);

}
