package com.sqlapk.medicine.module.patient.dal.mysql.homeeducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeeducation.HomeEducationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 居家用药教育 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface HomeEducationMapper extends BaseMapperX<HomeEducationDO> {

    default PageResult<HomeEducationDO> selectPage(HomeEducationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HomeEducationDO>()
                .eqIfPresent(HomeEducationDO::getHomeId, reqVO.getHomeId())
                .betweenIfPresent(HomeEducationDO::getEducationDate, reqVO.getEducationDate())
                .eqIfPresent(HomeEducationDO::getDiagnosisAndMedication, reqVO.getDiagnosisAndMedication())
                .eqIfPresent(HomeEducationDO::getKeyPoints, reqVO.getKeyPoints())
                .betweenIfPresent(HomeEducationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(HomeEducationDO::getId));
    }

    default List<HomeEducationDO> selectList(HomeEducationExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<HomeEducationDO>()
                .eqIfPresent(HomeEducationDO::getHomeId, reqVO.getHomeId())
                .betweenIfPresent(HomeEducationDO::getEducationDate, reqVO.getEducationDate())
                .eqIfPresent(HomeEducationDO::getDiagnosisAndMedication, reqVO.getDiagnosisAndMedication())
                .eqIfPresent(HomeEducationDO::getKeyPoints, reqVO.getKeyPoints())
                .betweenIfPresent(HomeEducationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(HomeEducationDO::getId));
    }

}
