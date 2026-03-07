package com.sqlapk.medicine.module.drugs.convert.drug;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsCreateReqVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsExcelVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsRespVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsUpdateReqVO;
import com.sqlapk.medicine.module.drugs.dal.dataobject.drug.DrugsDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 药品 Convert
 *
 * @author 管理员
 */
@Mapper
public interface DrugsConvert {

    DrugsConvert INSTANCE = Mappers.getMapper(DrugsConvert.class);

    DrugsDO convert(DrugsCreateReqVO bean);

    DrugsDO convert(DrugsUpdateReqVO bean);

    DrugsRespVO convert(DrugsDO bean);

    List<DrugsRespVO> convertList(List<DrugsDO> list);

    PageResult<DrugsRespVO> convertPage(PageResult<DrugsDO> page);

    List<DrugsExcelVO> convertList02(List<DrugsDO> list);

}
