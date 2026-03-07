package com.sqlapk.medicine.module.patient.dal.mysql.outpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatient.OutpatientDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 门诊药学服务 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface OutpatientMapper extends BaseMapperX<OutpatientDO> {

    default PageResult<OutpatientDO> selectPage(OutpatientPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OutpatientDO>()
                .eqIfPresent(OutpatientDO::getDoctorId, reqVO.getDoctorId())
                .eqIfPresent(OutpatientDO::getPatientId, reqVO.getPatientId())
                .eqIfPresent(OutpatientDO::getAllergyHistory, reqVO.getAllergyHistory())
                .eqIfPresent(OutpatientDO::getMedicalHistory, reqVO.getMedicalHistory())
                .eqIfPresent(OutpatientDO::getFamilyHistory, reqVO.getFamilyHistory())
                .eqIfPresent(OutpatientDO::getPersonalHistory, reqVO.getPersonalHistory())
                .eqIfPresent(OutpatientDO::getComment, reqVO.getComment())
                .betweenIfPresent(OutpatientDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OutpatientDO::getId));
    }

    default List<OutpatientDO> selectList(OutpatientExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<OutpatientDO>()
                .eqIfPresent(OutpatientDO::getDoctorId, reqVO.getDoctorId())
                .eqIfPresent(OutpatientDO::getPatientId, reqVO.getPatientId())
                .eqIfPresent(OutpatientDO::getAllergyHistory, reqVO.getAllergyHistory())
                .eqIfPresent(OutpatientDO::getMedicalHistory, reqVO.getMedicalHistory())
                .eqIfPresent(OutpatientDO::getFamilyHistory, reqVO.getFamilyHistory())
                .eqIfPresent(OutpatientDO::getPersonalHistory, reqVO.getPersonalHistory())
                .eqIfPresent(OutpatientDO::getComment, reqVO.getComment())
                .betweenIfPresent(OutpatientDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OutpatientDO::getId));
    }

}
