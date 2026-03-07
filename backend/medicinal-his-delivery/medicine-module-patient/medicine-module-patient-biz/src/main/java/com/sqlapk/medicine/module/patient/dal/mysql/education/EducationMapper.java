package com.sqlapk.medicine.module.patient.dal.mysql.education;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.education.EducationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用药教育 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface EducationMapper extends BaseMapperX<EducationDO> {

    default PageResult<EducationDO> selectPage(EducationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EducationDO>()
                .likeIfPresent(EducationDO::getMedicineName, reqVO.getMedicineName())
                .eqIfPresent(EducationDO::getPatientId, reqVO.getPatientId())
                .eqIfPresent(EducationDO::getDisease, reqVO.getDisease())
                .eqIfPresent(EducationDO::getContent, reqVO.getContent())
                .eqIfPresent(EducationDO::getContentBasis, reqVO.getContentBasis())
                .betweenIfPresent(EducationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(EducationDO::getId));
    }

    default List<EducationDO> selectList(EducationExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<EducationDO>()
                .likeIfPresent(EducationDO::getMedicineName, reqVO.getMedicineName())
                .eqIfPresent(EducationDO::getPatientId, reqVO.getPatientId())
                .eqIfPresent(EducationDO::getDisease, reqVO.getDisease())
                .eqIfPresent(EducationDO::getContent, reqVO.getContent())
                .eqIfPresent(EducationDO::getContentBasis, reqVO.getContentBasis())
                .betweenIfPresent(EducationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(EducationDO::getId));
    }

}
