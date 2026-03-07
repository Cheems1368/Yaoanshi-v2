package com.sqlapk.medicine.module.patient.dal.mysql.inpatienteducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatienteducation.InpatientEducationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 住院用药教育 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface InpatientEducationMapper extends BaseMapperX<InpatientEducationDO> {

    default PageResult<InpatientEducationDO> selectPage(InpatientEducationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InpatientEducationDO>()
                .eqIfPresent(InpatientEducationDO::getInpatientId, reqVO.getInpatientId())
                .betweenIfPresent(InpatientEducationDO::getEducationDate, reqVO.getEducationDate())
                .eqIfPresent(InpatientEducationDO::getDiagnosisAndMedication, reqVO.getDiagnosisAndMedication())
                .eqIfPresent(InpatientEducationDO::getKeyPoints, reqVO.getKeyPoints())
                .betweenIfPresent(InpatientEducationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InpatientEducationDO::getId));
    }

    default List<InpatientEducationDO> selectList(InpatientEducationExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<InpatientEducationDO>()
                .eqIfPresent(InpatientEducationDO::getInpatientId, reqVO.getInpatientId())
                .betweenIfPresent(InpatientEducationDO::getEducationDate, reqVO.getEducationDate())
                .eqIfPresent(InpatientEducationDO::getDiagnosisAndMedication, reqVO.getDiagnosisAndMedication())
                .eqIfPresent(InpatientEducationDO::getKeyPoints, reqVO.getKeyPoints())
                .betweenIfPresent(InpatientEducationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InpatientEducationDO::getId));
    }

}
