package com.sqlapk.medicine.module.system.convert.dict;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.system.api.dict.dto.DictDataRespDTO;
import com.sqlapk.medicine.module.system.controller.admin.dict.vo.data.DictDataCreateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.dict.vo.data.DictDataExcelVO;
import com.sqlapk.medicine.module.system.controller.admin.dict.vo.data.DictDataRespVO;
import com.sqlapk.medicine.module.system.controller.admin.dict.vo.data.DictDataSimpleRespVO;
import com.sqlapk.medicine.module.system.controller.admin.dict.vo.data.DictDataUpdateReqVO;
import com.sqlapk.medicine.module.system.dal.dataobject.dict.DictDataDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DictDataConvert {

    DictDataConvert INSTANCE = Mappers.getMapper(DictDataConvert.class);

    List<DictDataSimpleRespVO> convertList(List<DictDataDO> list);

    DictDataRespVO convert(DictDataDO bean);

    PageResult<DictDataRespVO> convertPage(PageResult<DictDataDO> page);

    DictDataDO convert(DictDataUpdateReqVO bean);

    DictDataDO convert(DictDataCreateReqVO bean);

    List<DictDataExcelVO> convertList02(List<DictDataDO> bean);

    DictDataRespDTO convert02(DictDataDO bean);

}
