package com.sqlapk.medicine.module.patient.convert.examinations;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.examinations.ExaminationsDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 检查项目 Convert
 *
 * @author 管理员
 */
@Mapper
public interface ExaminationsConvert {

    ExaminationsConvert INSTANCE = Mappers.getMapper(ExaminationsConvert.class);

    ExaminationsDO convert(ExaminationsCreateReqVO bean);

    ExaminationsDO convert(ExaminationsUpdateReqVO bean);

    ExaminationsRespVO convert(ExaminationsDO bean);

    List<ExaminationsRespVO> convertList(List<ExaminationsDO> list);

    PageResult<ExaminationsRespVO> convertPage(PageResult<ExaminationsDO> page);

    List<ExaminationsExcelVO> convertList02(List<ExaminationsDO> list);

}
