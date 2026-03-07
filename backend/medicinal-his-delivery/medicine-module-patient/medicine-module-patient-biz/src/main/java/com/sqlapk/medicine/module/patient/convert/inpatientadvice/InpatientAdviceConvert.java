package com.sqlapk.medicine.module.patient.convert.inpatientadvice;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientadvice.InpatientAdviceDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 住院药学不合理用药 Convert
 *
 * @author 管理员
 */
@Mapper
public interface InpatientAdviceConvert {

    InpatientAdviceConvert INSTANCE = Mappers.getMapper(InpatientAdviceConvert.class);

    InpatientAdviceDO convert(InpatientAdviceCreateReqVO bean);

    InpatientAdviceDO convert(InpatientAdviceUpdateReqVO bean);

    InpatientAdviceRespVO convert(InpatientAdviceDO bean);

    List<InpatientAdviceRespVO> convertList(List<InpatientAdviceDO> list);

    PageResult<InpatientAdviceRespVO> convertPage(PageResult<InpatientAdviceDO> page);

    List<InpatientAdviceExcelVO> convertList02(List<InpatientAdviceDO> list);

}
