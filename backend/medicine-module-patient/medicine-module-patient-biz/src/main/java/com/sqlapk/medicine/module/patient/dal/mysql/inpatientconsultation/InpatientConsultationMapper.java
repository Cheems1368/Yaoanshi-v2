package com.sqlapk.medicine.module.patient.dal.mysql.inpatientconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientconsultation.InpatientConsultationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 主要用药咨询 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface InpatientConsultationMapper extends BaseMapperX<InpatientConsultationDO> {

    default PageResult<InpatientConsultationDO> selectPage(InpatientConsultationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InpatientConsultationDO>()
                .eqIfPresent(InpatientConsultationDO::getInpatientId, reqVO.getInpatientId())
                .betweenIfPresent(InpatientConsultationDO::getConsultationDate, reqVO.getConsultationDate())
                .eqIfPresent(InpatientConsultationDO::getConsultationPoints, reqVO.getConsultationPoints())
                .eqIfPresent(InpatientConsultationDO::getReplyContent, reqVO.getReplyContent())
                .betweenIfPresent(InpatientConsultationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InpatientConsultationDO::getId));
    }

    default List<InpatientConsultationDO> selectList(InpatientConsultationExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<InpatientConsultationDO>()
                .eqIfPresent(InpatientConsultationDO::getInpatientId, reqVO.getInpatientId())
                .betweenIfPresent(InpatientConsultationDO::getConsultationDate, reqVO.getConsultationDate())
                .eqIfPresent(InpatientConsultationDO::getConsultationPoints, reqVO.getConsultationPoints())
                .eqIfPresent(InpatientConsultationDO::getReplyContent, reqVO.getReplyContent())
                .betweenIfPresent(InpatientConsultationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InpatientConsultationDO::getId));
    }

}
