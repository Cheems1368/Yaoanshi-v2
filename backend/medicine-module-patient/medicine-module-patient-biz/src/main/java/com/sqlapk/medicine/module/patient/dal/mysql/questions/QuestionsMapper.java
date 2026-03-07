package com.sqlapk.medicine.module.patient.dal.mysql.questions;

import java.util.*;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.module.patient.dal.dataobject.questions.QuestionsDO;
import org.apache.ibatis.annotations.Mapper;
import com.sqlapk.medicine.module.patient.controller.admin.questions.vo.*;

/**
 * 问卷问题 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface QuestionsMapper extends BaseMapperX<QuestionsDO> {

    default PageResult<QuestionsDO> selectPage(QuestionsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<QuestionsDO>()
                .eqIfPresent(QuestionsDO::getContent, reqVO.getContent())
                .eqIfPresent(QuestionsDO::getSurveyId, reqVO.getSurveyId())
                .betweenIfPresent(QuestionsDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(QuestionsDO::getId));
    }

    default List<QuestionsDO> selectList(QuestionsExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<QuestionsDO>()
                .eqIfPresent(QuestionsDO::getContent, reqVO.getContent())
                .eqIfPresent(QuestionsDO::getSurveyId, reqVO.getSurveyId())
                .betweenIfPresent(QuestionsDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(QuestionsDO::getId));
    }

}
