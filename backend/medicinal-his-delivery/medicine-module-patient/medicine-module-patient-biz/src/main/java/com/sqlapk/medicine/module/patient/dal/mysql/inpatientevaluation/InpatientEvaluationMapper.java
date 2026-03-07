package com.sqlapk.medicine.module.patient.dal.mysql.inpatientevaluation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientevaluation.InpatientEvaluationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 住院药学用药评估 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface InpatientEvaluationMapper extends BaseMapperX<InpatientEvaluationDO> {

    default PageResult<InpatientEvaluationDO> selectPage(InpatientEvaluationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InpatientEvaluationDO>()
                .eqIfPresent(InpatientEvaluationDO::getInpatientId, reqVO.getInpatientId())
                .betweenIfPresent(InpatientEvaluationDO::getEvaluationDate, reqVO.getEvaluationDate())
                .eqIfPresent(InpatientEvaluationDO::getTargetDrug, reqVO.getTargetDrug())
                .eqIfPresent(InpatientEvaluationDO::getSymptom, reqVO.getSymptom())
                .eqIfPresent(InpatientEvaluationDO::getResult, reqVO.getResult())
                .eqIfPresent(InpatientEvaluationDO::getMedicationSuggestion, reqVO.getMedicationSuggestion())
                .betweenIfPresent(InpatientEvaluationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InpatientEvaluationDO::getId));
    }

    default List<InpatientEvaluationDO> selectList(InpatientEvaluationExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<InpatientEvaluationDO>()
                .eqIfPresent(InpatientEvaluationDO::getInpatientId, reqVO.getInpatientId())
                .betweenIfPresent(InpatientEvaluationDO::getEvaluationDate, reqVO.getEvaluationDate())
                .eqIfPresent(InpatientEvaluationDO::getTargetDrug, reqVO.getTargetDrug())
                .eqIfPresent(InpatientEvaluationDO::getSymptom, reqVO.getSymptom())
                .eqIfPresent(InpatientEvaluationDO::getResult, reqVO.getResult())
                .eqIfPresent(InpatientEvaluationDO::getMedicationSuggestion, reqVO.getMedicationSuggestion())
                .betweenIfPresent(InpatientEvaluationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InpatientEvaluationDO::getId));
    }

}
