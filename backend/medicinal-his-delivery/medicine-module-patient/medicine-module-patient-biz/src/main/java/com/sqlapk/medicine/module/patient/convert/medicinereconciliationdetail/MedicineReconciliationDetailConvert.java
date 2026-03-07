package com.sqlapk.medicine.module.patient.convert.medicinereconciliationdetail;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliationdetail.MedicineReconciliationDetailDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 药物重整剂量 Convert
 *
 * @author 管理员
 */
@Mapper
public interface MedicineReconciliationDetailConvert {

    MedicineReconciliationDetailConvert INSTANCE = Mappers.getMapper(MedicineReconciliationDetailConvert.class);

    MedicineReconciliationDetailDO convert(MedicineReconciliationDetailCreateReqVO bean);

    MedicineReconciliationDetailDO convert(MedicineReconciliationDetailUpdateReqVO bean);

    MedicineReconciliationDetailRespVO convert(MedicineReconciliationDetailDO bean);

    List<MedicineReconciliationDetailRespVO> convertList(List<MedicineReconciliationDetailDO> list);

    PageResult<MedicineReconciliationDetailRespVO> convertPage(PageResult<MedicineReconciliationDetailDO> page);

    List<MedicineReconciliationDetailExcelVO> convertList02(List<MedicineReconciliationDetailDO> list);

}
