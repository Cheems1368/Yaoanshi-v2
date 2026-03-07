package com.sqlapk.medicine.module.patient.convert.outpatientmedicine;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientmedicine.OutpatientMedicineDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 药学门诊 Convert
 *
 * @author 管理员
 */
@Mapper
public interface OutpatientMedicineConvert {

    OutpatientMedicineConvert INSTANCE = Mappers.getMapper(OutpatientMedicineConvert.class);

    OutpatientMedicineDO convert(OutpatientMedicineCreateReqVO bean);

    OutpatientMedicineDO convert(OutpatientMedicineUpdateReqVO bean);

    OutpatientMedicineRespVO convert(OutpatientMedicineDO bean);

    List<OutpatientMedicineRespVO> convertList(List<OutpatientMedicineDO> list);

    PageResult<OutpatientMedicineRespVO> convertPage(PageResult<OutpatientMedicineDO> page);

    List<OutpatientMedicineExcelVO> convertList02(List<OutpatientMedicineDO> list);

}
