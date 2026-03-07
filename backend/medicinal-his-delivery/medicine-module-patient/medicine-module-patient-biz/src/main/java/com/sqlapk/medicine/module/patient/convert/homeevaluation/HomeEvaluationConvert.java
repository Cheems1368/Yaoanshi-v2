package com.sqlapk.medicine.module.patient.convert.homeevaluation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeevaluation.HomeEvaluationDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 居家随访评估 Convert
 *
 * @author 管理员
 */
@Mapper
public interface HomeEvaluationConvert {

    HomeEvaluationConvert INSTANCE = Mappers.getMapper(HomeEvaluationConvert.class);

    HomeEvaluationDO convert(HomeEvaluationCreateReqVO bean);

    HomeEvaluationDO convert(HomeEvaluationUpdateReqVO bean);

    HomeEvaluationRespVO convert(HomeEvaluationDO bean);

    List<HomeEvaluationRespVO> convertList(List<HomeEvaluationDO> list);

    PageResult<HomeEvaluationRespVO> convertPage(PageResult<HomeEvaluationDO> page);

    List<HomeEvaluationExcelVO> convertList02(List<HomeEvaluationDO> list);

}
