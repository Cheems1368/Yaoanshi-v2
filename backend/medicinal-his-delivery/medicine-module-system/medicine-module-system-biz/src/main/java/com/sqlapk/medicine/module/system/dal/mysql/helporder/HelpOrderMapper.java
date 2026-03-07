package com.sqlapk.medicine.module.system.dal.mysql.helporder;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderPageReqDTO;
import com.sqlapk.medicine.module.system.dal.dataobject.helporder.HelpOrderDO;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 工单 Mapper
 *
 * @author sqlapk
 */
@Mapper
public interface HelpOrderMapper extends BaseMapperX<HelpOrderDO> {

    default PageResult<HelpOrderDO> selectPage(HelpOrderPageReqDTO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HelpOrderDO>()
                .eqIfPresent(HelpOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(HelpOrderDO::getPatientId, reqVO.getPatientId())
                .eqIfPresent(HelpOrderDO::getAssignedPharmacistId, reqVO.getAssignedPharmacistId())
                .eqIfPresent(HelpOrderDO::getAssignedDoctorId, reqVO.getAssignedDoctorId())
                .likeIfPresent(HelpOrderDO::getTitle, reqVO.getTitle())
                .betweenIfPresent(HelpOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(HelpOrderDO::getId));
    }

    default List<HelpOrderDO> selectList(List<Long> ids) {
        return selectList(new LambdaQueryWrapperX<HelpOrderDO>()
                .inIfPresent(HelpOrderDO::getId, ids));
    }

    default List<HelpOrderDO> selectByPatientId(Long patientId) {
        return selectList(new LambdaQueryWrapperX<HelpOrderDO>()
                .eq(HelpOrderDO::getPatientId, patientId)
                .orderByDesc(HelpOrderDO::getCreateTime));
    }

    default List<HelpOrderDO> selectByPharmacistId(Long pharmacistId) {
        return selectList(new LambdaQueryWrapperX<HelpOrderDO>()
                .eq(HelpOrderDO::getAssignedPharmacistId, pharmacistId)
                .orderByDesc(HelpOrderDO::getCreateTime));
    }

    default List<HelpOrderDO> selectByDoctorId(Long doctorId) {
        return selectList(new LambdaQueryWrapperX<HelpOrderDO>()
                .eq(HelpOrderDO::getAssignedDoctorId, doctorId)
                .orderByDesc(HelpOrderDO::getCreateTime));
    }

    default List<HelpOrderDO> selectByStatus(HelpOrderStatus status) {
        return selectList(new LambdaQueryWrapperX<HelpOrderDO>()
                .eq(HelpOrderDO::getStatus, status)
                .orderByDesc(HelpOrderDO::getCreateTime));
    }

    default long selectCountByStatus(HelpOrderStatus status) {
        return selectCount(new LambdaQueryWrapperX<HelpOrderDO>()
                .eq(HelpOrderDO::getStatus, status));
    }

}

