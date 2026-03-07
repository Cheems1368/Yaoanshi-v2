package com.sqlapk.medicine.module.patient.dal.mysql.inpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatient.InpatientDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 住院药学服务 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface InpatientMapper extends BaseMapperX<InpatientDO> {

    default PageResult<InpatientDO> selectPage(InpatientPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InpatientDO>()
                .eqIfPresent(InpatientDO::getHospitalNumber, reqVO.getHospitalNumber())
                .eqIfPresent(InpatientDO::getBedNumber, reqVO.getBedNumber())
                .likeIfPresent(InpatientDO::getName, reqVO.getName())
                .eqIfPresent(InpatientDO::getGender, reqVO.getGender())
                .betweenIfPresent(InpatientDO::getAdmissionDate, reqVO.getAdmissionDate())
                .eqIfPresent(InpatientDO::getDiagnosis, reqVO.getDiagnosis())
                .eqIfPresent(InpatientDO::getCompliance, reqVO.getCompliance())
                .eqIfPresent(InpatientDO::getPatientCondition, reqVO.getPatientCondition())
                .eqIfPresent(InpatientDO::getSpecialPhysiologicalStatus, reqVO.getSpecialPhysiologicalStatus())
                .eqIfPresent(InpatientDO::getAdjustmentPlan, reqVO.getAdjustmentPlan())
                .betweenIfPresent(InpatientDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InpatientDO::getId));
    }

    default List<InpatientDO> selectList(InpatientExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<InpatientDO>()
                .eqIfPresent(InpatientDO::getHospitalNumber, reqVO.getHospitalNumber())
                .eqIfPresent(InpatientDO::getBedNumber, reqVO.getBedNumber())
                .likeIfPresent(InpatientDO::getName, reqVO.getName())
                .eqIfPresent(InpatientDO::getGender, reqVO.getGender())
                .betweenIfPresent(InpatientDO::getAdmissionDate, reqVO.getAdmissionDate())
                .eqIfPresent(InpatientDO::getDiagnosis, reqVO.getDiagnosis())
                .eqIfPresent(InpatientDO::getCompliance, reqVO.getCompliance())
                .eqIfPresent(InpatientDO::getPatientCondition, reqVO.getPatientCondition())
                .eqIfPresent(InpatientDO::getSpecialPhysiologicalStatus, reqVO.getSpecialPhysiologicalStatus())
                .eqIfPresent(InpatientDO::getAdjustmentPlan, reqVO.getAdjustmentPlan())
                .betweenIfPresent(InpatientDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InpatientDO::getId));
    }

}
