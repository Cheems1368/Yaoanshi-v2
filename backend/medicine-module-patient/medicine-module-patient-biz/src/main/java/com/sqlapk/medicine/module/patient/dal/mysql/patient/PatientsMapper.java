package com.sqlapk.medicine.module.patient.dal.mysql.patient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 患者 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface PatientsMapper extends BaseMapperX<PatientsDO> {

    default PageResult<PatientsDO> selectPage(PatientsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PatientsDO>()
                .eqIfPresent(PatientsDO::getUserId, reqVO.getUserId())
                .eqIfPresent(PatientsDO::getAddress, reqVO.getAddress())
                .eqIfPresent(PatientsDO::getInsuranceCardNumber, reqVO.getInsuranceCardNumber())
                .eqIfPresent(PatientsDO::getAllergyHistory, reqVO.getAllergyHistory())
                .eqIfPresent(PatientsDO::getMedicalHistory, reqVO.getMedicalHistory())
                .eqIfPresent(PatientsDO::getFamilyHistory, reqVO.getFamilyHistory())
                .eqIfPresent(PatientsDO::getPersonalHistory, reqVO.getPersonalHistory())
                .betweenIfPresent(PatientsDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(PatientsDO::getPatientType, reqVO.getPatientType())
                .eqIfPresent(PatientsDO::getInsurance, reqVO.getInsurance())
                .orderByDesc(PatientsDO::getId));
    }

    default List<PatientsDO> selectList(PatientsExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PatientsDO>()
                .eqIfPresent(PatientsDO::getUserId, reqVO.getUserId())
                .eqIfPresent(PatientsDO::getAddress, reqVO.getAddress())
                .eqIfPresent(PatientsDO::getInsuranceCardNumber, reqVO.getInsuranceCardNumber())
                .eqIfPresent(PatientsDO::getAllergyHistory, reqVO.getAllergyHistory())
                .eqIfPresent(PatientsDO::getMedicalHistory, reqVO.getMedicalHistory())
                .eqIfPresent(PatientsDO::getFamilyHistory, reqVO.getFamilyHistory())
                .eqIfPresent(PatientsDO::getPersonalHistory, reqVO.getPersonalHistory())
                .betweenIfPresent(PatientsDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(PatientsDO::getPatientType, reqVO.getPatientType())
                .eqIfPresent(PatientsDO::getInsurance, reqVO.getInsurance())
                .orderByDesc(PatientsDO::getId));
    }

    /**
     * 根据用户ID查询患者信息
     *
     * @param userId 用户ID
     * @return 患者信息
     */
    default PatientsDO selectByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapperX<PatientsDO>()
                .eq(PatientsDO::getUserId, userId));
    }

}
