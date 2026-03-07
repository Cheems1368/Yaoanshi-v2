package com.sqlapk.medicine.module.patient.convert.prescriptiondetail;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptiondetail.PrescriptionDetailDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 患者处方 Convert
 *
 * @author 管理员
 */
@Mapper
public interface PrescriptionDetailConvert {

    PrescriptionDetailConvert INSTANCE = Mappers.getMapper(PrescriptionDetailConvert.class);

    PrescriptionDetailDO convert(PrescriptionDetailCreateReqVO bean);

    PrescriptionDetailDO convert(PrescriptionDetailUpdateReqVO bean);

    PrescriptionDetailRespVO convert(PrescriptionDetailDO bean);

    List<PrescriptionDetailRespVO> convertList(List<PrescriptionDetailDO> list);

    PageResult<PrescriptionDetailRespVO> convertPage(PageResult<PrescriptionDetailDO> page);

    List<PrescriptionDetailExcelVO> convertList02(List<PrescriptionDetailDO> list);

}
