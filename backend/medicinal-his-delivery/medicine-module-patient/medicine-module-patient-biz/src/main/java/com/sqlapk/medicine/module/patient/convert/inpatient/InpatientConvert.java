package com.sqlapk.medicine.module.patient.convert.inpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatient.InpatientDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 住院药学服务 Convert
 *
 * @author 管理员
 */
@Mapper
public interface InpatientConvert {

    InpatientConvert INSTANCE = Mappers.getMapper(InpatientConvert.class);

    InpatientDO convert(InpatientCreateReqVO bean);

    InpatientDO convert(InpatientUpdateReqVO bean);

    InpatientRespVO convert(InpatientDO bean);

    List<InpatientRespVO> convertList(List<InpatientDO> list);

    PageResult<InpatientRespVO> convertPage(PageResult<InpatientDO> page);

    List<InpatientExcelVO> convertList02(List<InpatientDO> list);

}
