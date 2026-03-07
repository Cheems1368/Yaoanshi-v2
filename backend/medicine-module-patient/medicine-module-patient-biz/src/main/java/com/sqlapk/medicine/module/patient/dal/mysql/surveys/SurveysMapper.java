package com.sqlapk.medicine.module.patient.dal.mysql.surveys;

import java.util.*;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.module.patient.dal.dataobject.surveys.SurveysDO;
import org.apache.ibatis.annotations.Mapper;
import com.sqlapk.medicine.module.patient.controller.admin.surveys.vo.*;

/**
 * 问卷 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface SurveysMapper extends BaseMapperX<SurveysDO> {

    default PageResult<SurveysDO> selectPage(SurveysPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SurveysDO>()
                .eqIfPresent(SurveysDO::getTitle, reqVO.getTitle())
                .eqIfPresent(SurveysDO::getDescription, reqVO.getDescription())
                .betweenIfPresent(SurveysDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SurveysDO::getId));
    }

    default List<SurveysDO> selectList(SurveysExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SurveysDO>()
                .eqIfPresent(SurveysDO::getTitle, reqVO.getTitle())
                .eqIfPresent(SurveysDO::getDescription, reqVO.getDescription())
                .betweenIfPresent(SurveysDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SurveysDO::getId));
    }

}
