package com.sqlapk.medicine.module.patient.convert.homemedicine;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homemedicine.HomeMedicineDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用药清单 Convert
 *
 * @author 管理员
 */
@Mapper
public interface HomeMedicineConvert {

    HomeMedicineConvert INSTANCE = Mappers.getMapper(HomeMedicineConvert.class);

    HomeMedicineDO convert(HomeMedicineCreateReqVO bean);

    HomeMedicineDO convert(HomeMedicineUpdateReqVO bean);

    HomeMedicineRespVO convert(HomeMedicineDO bean);

    List<HomeMedicineRespVO> convertList(List<HomeMedicineDO> list);

    PageResult<HomeMedicineRespVO> convertPage(PageResult<HomeMedicineDO> page);

    List<HomeMedicineExcelVO> convertList02(List<HomeMedicineDO> list);

}
