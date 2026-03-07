package com.sqlapk.medicine.module.patient.convert.inpatientevaluation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientevaluation.InpatientEvaluationDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 住院药学用药评估 Convert
 *
 * @author 管理员 */
@Mapper(componentModel = "spring")
public interface InpatientEvaluationConvert {

    InpatientEvaluationConvert INSTANCE = Mappers.getMapper(InpatientEvaluationConvert.class);

    InpatientEvaluationDO convert(InpatientEvaluationCreateReqVO bean);

    InpatientEvaluationDO convert(InpatientEvaluationUpdateReqVO bean);

    InpatientEvaluationRespVO convert(InpatientEvaluationDO bean);

    List<InpatientEvaluationRespVO> convertList(List<InpatientEvaluationDO> list);

    PageResult<InpatientEvaluationRespVO> convertPage(PageResult<InpatientEvaluationDO> page);

    List<InpatientEvaluationExcelVO> convertList02(List<InpatientEvaluationDO> list);

}
