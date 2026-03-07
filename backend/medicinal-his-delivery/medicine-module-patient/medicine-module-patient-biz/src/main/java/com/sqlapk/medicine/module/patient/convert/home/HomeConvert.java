package com.sqlapk.medicine.module.patient.convert.home;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.home.HomeDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 居家药学服务 Convert
 *
 * @author 管理员
 */
@Mapper
public interface HomeConvert {

    HomeConvert INSTANCE = Mappers.getMapper(HomeConvert.class);

    HomeDO convert(HomeCreateReqVO bean);

    HomeDO convert(HomeUpdateReqVO bean);

    HomeRespVO convert(HomeDO bean);

    List<HomeRespVO> convertList(List<HomeDO> list);

    PageResult<HomeRespVO> convertPage(PageResult<HomeDO> page);

    List<HomeExcelVO> convertList02(List<HomeDO> list);

}
