package com.sqlapk.medicine.module.patient.dal.mysql.prescriptions;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptions.PrescriptionsDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 处方 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface PrescriptionsMapper extends BaseMapperX<PrescriptionsDO> {

    default PageResult<PrescriptionsDO> selectPage(PrescriptionsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PrescriptionsDO>()
                .eqIfPresent(PrescriptionsDO::getPatientId, reqVO.getPatientId())
                .betweenIfPresent(PrescriptionsDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(PrescriptionsDO::getType, reqVO.getType())
                .eqIfPresent(PrescriptionsDO::getDoctorId, reqVO.getDoctorId())
                .eqIfPresent(PrescriptionsDO::getDiagnosis, reqVO.getDiagnosis())
                .orderByDesc(PrescriptionsDO::getId));
    }

    default List<PrescriptionsDO> selectList(PrescriptionsExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PrescriptionsDO>()
                .eqIfPresent(PrescriptionsDO::getPatientId, reqVO.getPatientId())
                .betweenIfPresent(PrescriptionsDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(PrescriptionsDO::getType, reqVO.getType())
                .eqIfPresent(PrescriptionsDO::getDoctorId, reqVO.getDoctorId())
                .eqIfPresent(PrescriptionsDO::getDiagnosis, reqVO.getDiagnosis())
                .orderByDesc(PrescriptionsDO::getId));
    }

}
