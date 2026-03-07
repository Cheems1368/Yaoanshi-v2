package com.sqlapk.medicine.module.patient.convert.questions;

import java.util.*;

import com.sqlapk.medicine.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.sqlapk.medicine.module.patient.controller.admin.questions.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.questions.QuestionsDO;

/**
 * 问卷问题 Convert
 *
 * @author 管理员
 */
@Mapper
public interface QuestionsConvert {

    QuestionsConvert INSTANCE = Mappers.getMapper(QuestionsConvert.class);

    QuestionsDO convert(QuestionsCreateReqVO bean);

    QuestionsDO convert(QuestionsUpdateReqVO bean);

    QuestionsRespVO convert(QuestionsDO bean);

    List<QuestionsRespVO> convertList(List<QuestionsDO> list);

    PageResult<QuestionsRespVO> convertPage(PageResult<QuestionsDO> page);

    List<QuestionsExcelVO> convertList02(List<QuestionsDO> list);

}
