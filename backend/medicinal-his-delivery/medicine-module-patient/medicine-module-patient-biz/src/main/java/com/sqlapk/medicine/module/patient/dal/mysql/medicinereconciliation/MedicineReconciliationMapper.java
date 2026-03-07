package com.sqlapk.medicine.module.patient.dal.mysql.medicinereconciliation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliation.MedicineReconciliationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 药物重整剂量 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface MedicineReconciliationMapper extends BaseMapperX<MedicineReconciliationDO> {

    default PageResult<MedicineReconciliationDO> selectPage(MedicineReconciliationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MedicineReconciliationDO>()
                .eqIfPresent(MedicineReconciliationDO::getPatientId, reqVO.getPatientId())
                .eqIfPresent(MedicineReconciliationDO::getDoctorId, reqVO.getDoctorId())
                .likeIfPresent(MedicineReconciliationDO::getName, reqVO.getName())
                .eqIfPresent(MedicineReconciliationDO::getGender, reqVO.getGender())
                .eqIfPresent(MedicineReconciliationDO::getBirthday, reqVO.getBirthday())
                .eqIfPresent(MedicineReconciliationDO::getPhone, reqVO.getPhone())
                .betweenIfPresent(MedicineReconciliationDO::getClinicTime, reqVO.getClinicTime())
                .betweenIfPresent(MedicineReconciliationDO::getDischargeTime, reqVO.getDischargeTime())
                .eqIfPresent(MedicineReconciliationDO::getDiagnose, reqVO.getDiagnose())
                .eqIfPresent(MedicineReconciliationDO::getAllergyHistory, reqVO.getAllergyHistory())
                .eqIfPresent(MedicineReconciliationDO::getMedicineQuestion, reqVO.getMedicineQuestion())
                .eqIfPresent(MedicineReconciliationDO::getMedicineDoctor, reqVO.getMedicineDoctor())
                .betweenIfPresent(MedicineReconciliationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MedicineReconciliationDO::getId));
    }

    default List<MedicineReconciliationDO> selectList(MedicineReconciliationExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MedicineReconciliationDO>()
                .eqIfPresent(MedicineReconciliationDO::getPatientId, reqVO.getPatientId())
                .eqIfPresent(MedicineReconciliationDO::getDoctorId, reqVO.getDoctorId())
                .likeIfPresent(MedicineReconciliationDO::getName, reqVO.getName())
                .eqIfPresent(MedicineReconciliationDO::getGender, reqVO.getGender())
                .eqIfPresent(MedicineReconciliationDO::getBirthday, reqVO.getBirthday())
                .eqIfPresent(MedicineReconciliationDO::getPhone, reqVO.getPhone())
                .betweenIfPresent(MedicineReconciliationDO::getClinicTime, reqVO.getClinicTime())
                .betweenIfPresent(MedicineReconciliationDO::getDischargeTime, reqVO.getDischargeTime())
                .eqIfPresent(MedicineReconciliationDO::getDiagnose, reqVO.getDiagnose())
                .eqIfPresent(MedicineReconciliationDO::getAllergyHistory, reqVO.getAllergyHistory())
                .eqIfPresent(MedicineReconciliationDO::getMedicineQuestion, reqVO.getMedicineQuestion())
                .eqIfPresent(MedicineReconciliationDO::getMedicineDoctor, reqVO.getMedicineDoctor())
                .betweenIfPresent(MedicineReconciliationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MedicineReconciliationDO::getId));
    }

}
