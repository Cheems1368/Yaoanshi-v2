package com.sqlapk.medicine.module.patient.convert.options;

import java.util.*;

import com.sqlapk.medicine.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.sqlapk.medicine.module.patient.controller.admin.options.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.options.OptionsDO;

/**
 * 问题选项 Convert
 *
 * @author 管理员 */
@Mapper(componentModel = "spring")
public interface OptionsConvert {

    OptionsConvert INSTANCE = Mappers.getMapper(OptionsConvert.class);

    OptionsDO convert(OptionsCreateReqVO bean);

    OptionsDO convert(OptionsUpdateReqVO bean);

    OptionsRespVO convert(OptionsDO bean);

    List<OptionsRespVO> convertList(List<OptionsDO> list);

    PageResult<OptionsRespVO> convertPage(PageResult<OptionsDO> page);

    List<OptionsExcelVO> convertList02(List<OptionsDO> list);

}
