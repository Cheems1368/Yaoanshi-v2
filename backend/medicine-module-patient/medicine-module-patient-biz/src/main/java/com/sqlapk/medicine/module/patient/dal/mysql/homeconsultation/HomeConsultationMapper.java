package com.sqlapk.medicine.module.patient.dal.mysql.homeconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeconsultation.HomeConsultationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 居家用药咨询 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface HomeConsultationMapper extends BaseMapperX<HomeConsultationDO> {

    default PageResult<HomeConsultationDO> selectPage(HomeConsultationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HomeConsultationDO>()
                .eqIfPresent(HomeConsultationDO::getHomeId, reqVO.getHomeId())
                .betweenIfPresent(HomeConsultationDO::getConsultationDate, reqVO.getConsultationDate())
                .eqIfPresent(HomeConsultationDO::getConsultationPoints, reqVO.getConsultationPoints())
                .eqIfPresent(HomeConsultationDO::getReplyContent, reqVO.getReplyContent())
                .betweenIfPresent(HomeConsultationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(HomeConsultationDO::getId));
    }

    default List<HomeConsultationDO> selectList(HomeConsultationExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<HomeConsultationDO>()
                .eqIfPresent(HomeConsultationDO::getHomeId, reqVO.getHomeId())
                .betweenIfPresent(HomeConsultationDO::getConsultationDate, reqVO.getConsultationDate())
                .eqIfPresent(HomeConsultationDO::getConsultationPoints, reqVO.getConsultationPoints())
                .eqIfPresent(HomeConsultationDO::getReplyContent, reqVO.getReplyContent())
                .betweenIfPresent(HomeConsultationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(HomeConsultationDO::getId));
    }

}
