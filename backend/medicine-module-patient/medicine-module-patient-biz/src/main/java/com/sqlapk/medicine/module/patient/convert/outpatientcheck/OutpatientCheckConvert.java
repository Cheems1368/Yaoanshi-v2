package com.sqlapk.medicine.module.patient.convert.outpatientcheck;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientcheck.OutpatientCheckDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 处方审核 Convert
 *
 * @author 管理员 */
@Mapper(componentModel = "spring")
public interface OutpatientCheckConvert {

    OutpatientCheckConvert INSTANCE = Mappers.getMapper(OutpatientCheckConvert.class);

    OutpatientCheckDO convert(OutpatientCheckCreateReqVO bean);

    OutpatientCheckDO convert(OutpatientCheckUpdateReqVO bean);

    OutpatientCheckRespVO convert(OutpatientCheckDO bean);

    List<OutpatientCheckRespVO> convertList(List<OutpatientCheckDO> list);

    PageResult<OutpatientCheckRespVO> convertPage(PageResult<OutpatientCheckDO> page);

    List<OutpatientCheckExcelVO> convertList02(List<OutpatientCheckDO> list);

}
