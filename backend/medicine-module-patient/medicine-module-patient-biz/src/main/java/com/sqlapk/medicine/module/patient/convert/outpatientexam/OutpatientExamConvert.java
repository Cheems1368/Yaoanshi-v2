package com.sqlapk.medicine.module.patient.convert.outpatientexam;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientexam.OutpatientExamDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 门诊药学检查项目明细?Convert
 *
 * @author 管理员 */
@Mapper(componentModel = "spring")
public interface OutpatientExamConvert {

    OutpatientExamConvert INSTANCE = Mappers.getMapper(OutpatientExamConvert.class);

    OutpatientExamDO convert(OutpatientExamCreateReqVO bean);

    OutpatientExamDO convert(OutpatientExamUpdateReqVO bean);

    OutpatientExamRespVO convert(OutpatientExamDO bean);

    List<OutpatientExamRespVO> convertList(List<OutpatientExamDO> list);

    PageResult<OutpatientExamRespVO> convertPage(PageResult<OutpatientExamDO> page);

    List<OutpatientExamExcelVO> convertList02(List<OutpatientExamDO> list);

}
