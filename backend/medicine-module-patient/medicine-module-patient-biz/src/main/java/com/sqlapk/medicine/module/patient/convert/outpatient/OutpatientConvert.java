package com.sqlapk.medicine.module.patient.convert.outpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatient.OutpatientDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 门诊药学服务 Convert
 *
 * @author 管理员 */
@Mapper(componentModel = "spring")
public interface OutpatientConvert {

    OutpatientConvert INSTANCE = Mappers.getMapper(OutpatientConvert.class);

    OutpatientDO convert(OutpatientCreateReqVO bean);

    OutpatientDO convert(OutpatientUpdateReqVO bean);

    OutpatientRespVO convert(OutpatientDO bean);

    List<OutpatientRespVO> convertList(List<OutpatientDO> list);

    PageResult<OutpatientRespVO> convertPage(PageResult<OutpatientDO> page);

    List<OutpatientExcelVO> convertList02(List<OutpatientDO> list);

}
