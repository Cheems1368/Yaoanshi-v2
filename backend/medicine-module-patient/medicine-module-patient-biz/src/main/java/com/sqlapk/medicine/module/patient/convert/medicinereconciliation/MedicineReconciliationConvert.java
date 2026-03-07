package com.sqlapk.medicine.module.patient.convert.medicinereconciliation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliation.MedicineReconciliationDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 药物重整剂量 Convert
 *
 * @author 管理员 */
@Mapper(componentModel = "spring")
public interface MedicineReconciliationConvert {

    MedicineReconciliationConvert INSTANCE = Mappers.getMapper(MedicineReconciliationConvert.class);

    MedicineReconciliationDO convert(MedicineReconciliationCreateReqVO bean);

    MedicineReconciliationDO convert(MedicineReconciliationUpdateReqVO bean);

    MedicineReconciliationRespVO convert(MedicineReconciliationDO bean);

    List<MedicineReconciliationRespVO> convertList(List<MedicineReconciliationDO> list);

    PageResult<MedicineReconciliationRespVO> convertPage(PageResult<MedicineReconciliationDO> page);

    List<MedicineReconciliationExcelVO> convertList02(List<MedicineReconciliationDO> list);

}
