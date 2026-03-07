package com.sqlapk.medicine.module.patient.convert.medicineconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicineconsultation.MedicineConsultationDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用药咨询 Convert
 *
 * @author 管理员
 */
@Mapper
public interface MedicineConsultationConvert {

    MedicineConsultationConvert INSTANCE = Mappers.getMapper(MedicineConsultationConvert.class);

    MedicineConsultationDO convert(MedicineConsultationCreateReqVO bean);

    MedicineConsultationDO convert(MedicineConsultationUpdateReqVO bean);

    MedicineConsultationRespVO convert(MedicineConsultationDO bean);

    List<MedicineConsultationRespVO> convertList(List<MedicineConsultationDO> list);

    PageResult<MedicineConsultationRespVO> convertPage(PageResult<MedicineConsultationDO> page);

    List<MedicineConsultationExcelVO> convertList02(List<MedicineConsultationDO> list);

}
