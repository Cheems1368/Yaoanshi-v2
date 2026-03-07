package com.sqlapk.medicine.module.patient.convert.inpatientround;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientround.InpatientRoundDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 住院药学查房记录 Convert
 *
 * @author 管理员 */
@Mapper(componentModel = "spring")
public interface InpatientRoundConvert {

    InpatientRoundConvert INSTANCE = Mappers.getMapper(InpatientRoundConvert.class);

    InpatientRoundDO convert(InpatientRoundCreateReqVO bean);

    InpatientRoundDO convert(InpatientRoundUpdateReqVO bean);

    InpatientRoundRespVO convert(InpatientRoundDO bean);

    List<InpatientRoundRespVO> convertList(List<InpatientRoundDO> list);

    PageResult<InpatientRoundRespVO> convertPage(PageResult<InpatientRoundDO> page);

    List<InpatientRoundExcelVO> convertList02(List<InpatientRoundDO> list);

}
