package com.sqlapk.medicine.module.patient.convert.homeconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeconsultation.HomeConsultationDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 居家用药咨询 Convert
 *
 * @author 管理员 */
@Mapper(componentModel = "spring")
public interface HomeConsultationConvert {

    HomeConsultationConvert INSTANCE = Mappers.getMapper(HomeConsultationConvert.class);

    HomeConsultationDO convert(HomeConsultationCreateReqVO bean);

    HomeConsultationDO convert(HomeConsultationUpdateReqVO bean);

    HomeConsultationRespVO convert(HomeConsultationDO bean);

    List<HomeConsultationRespVO> convertList(List<HomeConsultationDO> list);

    PageResult<HomeConsultationRespVO> convertPage(PageResult<HomeConsultationDO> page);

    List<HomeConsultationExcelVO> convertList02(List<HomeConsultationDO> list);

}
