package com.sqlapk.medicine.module.patient.dal.mysql.outpatientcheck;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientcheck.OutpatientCheckDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 处方审核 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface OutpatientCheckMapper extends BaseMapperX<OutpatientCheckDO> {

    default PageResult<OutpatientCheckDO> selectPage(OutpatientCheckPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OutpatientCheckDO>()
                .eqIfPresent(OutpatientCheckDO::getOutpatientId, reqVO.getOutpatientId())
                .likeIfPresent(OutpatientCheckDO::getMedicineName, reqVO.getMedicineName())
                .eqIfPresent(OutpatientCheckDO::getDoctorId, reqVO.getDoctorId())
                .eqIfPresent(OutpatientCheckDO::getPrescriptionId, reqVO.getPrescriptionId())
                .eqIfPresent(OutpatientCheckDO::getIssueDesc, reqVO.getIssueDesc())
                .eqIfPresent(OutpatientCheckDO::getIssueCode, reqVO.getIssueCode())
                .eqIfPresent(OutpatientCheckDO::getInterveneResult, reqVO.getInterveneResult())
                .betweenIfPresent(OutpatientCheckDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OutpatientCheckDO::getId));
    }

    default List<OutpatientCheckDO> selectList(OutpatientCheckExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<OutpatientCheckDO>()
                .eqIfPresent(OutpatientCheckDO::getOutpatientId, reqVO.getOutpatientId())
                .likeIfPresent(OutpatientCheckDO::getMedicineName, reqVO.getMedicineName())
                .eqIfPresent(OutpatientCheckDO::getDoctorId, reqVO.getDoctorId())
                .eqIfPresent(OutpatientCheckDO::getPrescriptionId, reqVO.getPrescriptionId())
                .eqIfPresent(OutpatientCheckDO::getIssueDesc, reqVO.getIssueDesc())
                .eqIfPresent(OutpatientCheckDO::getIssueCode, reqVO.getIssueCode())
                .eqIfPresent(OutpatientCheckDO::getInterveneResult, reqVO.getInterveneResult())
                .betweenIfPresent(OutpatientCheckDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OutpatientCheckDO::getId));
    }

}
