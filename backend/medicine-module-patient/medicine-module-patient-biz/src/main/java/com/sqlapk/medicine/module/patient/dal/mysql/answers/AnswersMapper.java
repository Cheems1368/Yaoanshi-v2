package com.sqlapk.medicine.module.patient.dal.mysql.answers;

import java.util.*;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.module.patient.dal.dataobject.answers.AnswersDO;
import org.apache.ibatis.annotations.Mapper;
import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.*;

/**
 * 问卷 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface AnswersMapper extends BaseMapperX<AnswersDO> {

    default PageResult<AnswersDO> selectPage(AnswersPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AnswersDO>()
                .eqIfPresent(AnswersDO::getUserId, reqVO.getUserId())
                .eqIfPresent(AnswersDO::getQuestionId, reqVO.getQuestionId())
                .eqIfPresent(AnswersDO::getOptionId, reqVO.getOptionId())
                .eqIfPresent(AnswersDO::getValue, reqVO.getValue())
                .betweenIfPresent(AnswersDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AnswersDO::getId));
    }

    default List<AnswersDO> selectList(AnswersExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<AnswersDO>()
                .eqIfPresent(AnswersDO::getUserId, reqVO.getUserId())
                .eqIfPresent(AnswersDO::getQuestionId, reqVO.getQuestionId())
                .eqIfPresent(AnswersDO::getOptionId, reqVO.getOptionId())
                .eqIfPresent(AnswersDO::getValue, reqVO.getValue())
                .betweenIfPresent(AnswersDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AnswersDO::getId));
    }

}
