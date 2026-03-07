package com.sqlapk.medicine.module.patient.dal.mysql.medicineconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicineconsultation.MedicineConsultationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用药咨询 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface MedicineConsultationMapper extends BaseMapperX<MedicineConsultationDO> {

    default PageResult<MedicineConsultationDO> selectPage(MedicineConsultationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MedicineConsultationDO>()
                .eqIfPresent(MedicineConsultationDO::getPatientId, reqVO.getPatientId())
                .eqIfPresent(MedicineConsultationDO::getQuestion, reqVO.getQuestion())
                .eqIfPresent(MedicineConsultationDO::getAnswer, reqVO.getAnswer())
                .eqIfPresent(MedicineConsultationDO::getConsultObject, reqVO.getConsultObject())
                .eqIfPresent(MedicineConsultationDO::getSpecialPatient, reqVO.getSpecialPatient())
                .eqIfPresent(MedicineConsultationDO::getType, reqVO.getType())
                .eqIfPresent(MedicineConsultationDO::getDuration, reqVO.getDuration())
                .eqIfPresent(MedicineConsultationDO::getDoctorId, reqVO.getDoctorId())
                .eqIfPresent(MedicineConsultationDO::getAnswerBasis, reqVO.getAnswerBasis())
                .eqIfPresent(MedicineConsultationDO::getComment, reqVO.getComment())
                .betweenIfPresent(MedicineConsultationDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(MedicineConsultationDO::getMedicineId, reqVO.getMedicineId())
                .orderByDesc(MedicineConsultationDO::getId));
    }

    default List<MedicineConsultationDO> selectList(MedicineConsultationExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MedicineConsultationDO>()
                .eqIfPresent(MedicineConsultationDO::getPatientId, reqVO.getPatientId())
                .eqIfPresent(MedicineConsultationDO::getQuestion, reqVO.getQuestion())
                .eqIfPresent(MedicineConsultationDO::getAnswer, reqVO.getAnswer())
                .eqIfPresent(MedicineConsultationDO::getConsultObject, reqVO.getConsultObject())
                .eqIfPresent(MedicineConsultationDO::getSpecialPatient, reqVO.getSpecialPatient())
                .eqIfPresent(MedicineConsultationDO::getType, reqVO.getType())
                .eqIfPresent(MedicineConsultationDO::getDuration, reqVO.getDuration())
                .eqIfPresent(MedicineConsultationDO::getDoctorId, reqVO.getDoctorId())
                .eqIfPresent(MedicineConsultationDO::getAnswerBasis, reqVO.getAnswerBasis())
                .eqIfPresent(MedicineConsultationDO::getComment, reqVO.getComment())
                .betweenIfPresent(MedicineConsultationDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(MedicineConsultationDO::getMedicineId, reqVO.getMedicineId())
                .orderByDesc(MedicineConsultationDO::getId));
    }

}
