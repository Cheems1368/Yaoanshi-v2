package com.sqlapk.medicine.module.patient.convert.prescriptionexaminations;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptionexaminations.PrescriptionExaminationsDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 处方检查项目关联?Convert
 *
 * @author 管理员 */
@Mapper(componentModel = "spring")
public interface PrescriptionExaminationsConvert {

    PrescriptionExaminationsConvert INSTANCE = Mappers.getMapper(PrescriptionExaminationsConvert.class);

    PrescriptionExaminationsDO convert(PrescriptionExaminationsCreateReqVO bean);

    PrescriptionExaminationsDO convert(PrescriptionExaminationsUpdateReqVO bean);

    PrescriptionExaminationsRespVO convert(PrescriptionExaminationsDO bean);

    List<PrescriptionExaminationsRespVO> convertList(List<PrescriptionExaminationsDO> list);

    PageResult<PrescriptionExaminationsRespVO> convertPage(PageResult<PrescriptionExaminationsDO> page);

    List<PrescriptionExaminationsExcelVO> convertList02(List<PrescriptionExaminationsDO> list);

}
