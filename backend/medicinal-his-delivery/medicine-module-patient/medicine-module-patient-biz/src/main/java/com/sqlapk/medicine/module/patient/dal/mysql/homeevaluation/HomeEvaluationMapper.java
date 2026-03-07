package com.sqlapk.medicine.module.patient.dal.mysql.homeevaluation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeevaluation.HomeEvaluationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 居家随访评估 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface HomeEvaluationMapper extends BaseMapperX<HomeEvaluationDO> {

    default PageResult<HomeEvaluationDO> selectPage(HomeEvaluationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HomeEvaluationDO>()
                .betweenIfPresent(HomeEvaluationDO::getEvaluationDate, reqVO.getEvaluationDate())
                .eqIfPresent(HomeEvaluationDO::getHomeId, reqVO.getHomeId())
                .eqIfPresent(HomeEvaluationDO::getSymptom, reqVO.getSymptom())
                .eqIfPresent(HomeEvaluationDO::getIndicator, reqVO.getIndicator())
                .eqIfPresent(HomeEvaluationDO::getTreatmentProblem, reqVO.getTreatmentProblem())
                .betweenIfPresent(HomeEvaluationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(HomeEvaluationDO::getId));
    }

    default List<HomeEvaluationDO> selectList(HomeEvaluationExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<HomeEvaluationDO>()
                .betweenIfPresent(HomeEvaluationDO::getEvaluationDate, reqVO.getEvaluationDate())
                .eqIfPresent(HomeEvaluationDO::getHomeId, reqVO.getHomeId())
                .eqIfPresent(HomeEvaluationDO::getSymptom, reqVO.getSymptom())
                .eqIfPresent(HomeEvaluationDO::getIndicator, reqVO.getIndicator())
                .eqIfPresent(HomeEvaluationDO::getTreatmentProblem, reqVO.getTreatmentProblem())
                .betweenIfPresent(HomeEvaluationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(HomeEvaluationDO::getId));
    }

}
