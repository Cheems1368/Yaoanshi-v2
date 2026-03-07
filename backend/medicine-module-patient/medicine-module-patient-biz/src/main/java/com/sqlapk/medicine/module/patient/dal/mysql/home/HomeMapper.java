package com.sqlapk.medicine.module.patient.dal.mysql.home;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomePageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.home.HomeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 居家药学服务 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface HomeMapper extends BaseMapperX<HomeDO> {

    default PageResult<HomeDO> selectPage(HomePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HomeDO>()
                .eqIfPresent(HomeDO::getDoctorId, reqVO.getDoctorId())
                .eqIfPresent(HomeDO::getPatientId, reqVO.getPatientId())
                .likeIfPresent(HomeDO::getName, reqVO.getName())
                .eqIfPresent(HomeDO::getInsurance, reqVO.getInsurance())
                .eqIfPresent(HomeDO::getGender, reqVO.getGender())
                .eqIfPresent(HomeDO::getBirthday, reqVO.getBirthday())
                .eqIfPresent(HomeDO::getAddress, reqVO.getAddress())
                .eqIfPresent(HomeDO::getPhone, reqVO.getPhone())
                .eqIfPresent(HomeDO::getInterviewLocation, reqVO.getInterviewLocation())
                .betweenIfPresent(HomeDO::getInterviewTime, reqVO.getInterviewTime())
                .eqIfPresent(HomeDO::getComplicatedDisease, reqVO.getComplicatedDisease())
                .eqIfPresent(HomeDO::getInterviewPurpose, reqVO.getInterviewPurpose())
                .betweenIfPresent(HomeDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(HomeDO::getId));
    }

    default List<HomeDO> selectList(HomeExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<HomeDO>()
                .eqIfPresent(HomeDO::getDoctorId, reqVO.getDoctorId())
                .eqIfPresent(HomeDO::getPatientId, reqVO.getPatientId())
                .likeIfPresent(HomeDO::getName, reqVO.getName())
                .eqIfPresent(HomeDO::getInsurance, reqVO.getInsurance())
                .eqIfPresent(HomeDO::getGender, reqVO.getGender())
                .eqIfPresent(HomeDO::getBirthday, reqVO.getBirthday())
                .eqIfPresent(HomeDO::getAddress, reqVO.getAddress())
                .eqIfPresent(HomeDO::getPhone, reqVO.getPhone())
                .eqIfPresent(HomeDO::getInterviewLocation, reqVO.getInterviewLocation())
                .betweenIfPresent(HomeDO::getInterviewTime, reqVO.getInterviewTime())
                .eqIfPresent(HomeDO::getComplicatedDisease, reqVO.getComplicatedDisease())
                .eqIfPresent(HomeDO::getInterviewPurpose, reqVO.getInterviewPurpose())
                .betweenIfPresent(HomeDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(HomeDO::getId));
    }

}
