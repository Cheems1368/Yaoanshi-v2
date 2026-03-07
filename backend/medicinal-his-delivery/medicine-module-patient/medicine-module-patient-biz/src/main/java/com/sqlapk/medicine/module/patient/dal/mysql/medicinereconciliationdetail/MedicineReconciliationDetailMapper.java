package com.sqlapk.medicine.module.patient.dal.mysql.medicinereconciliationdetail;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliationdetail.MedicineReconciliationDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 药物重整剂量 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface MedicineReconciliationDetailMapper extends BaseMapperX<MedicineReconciliationDetailDO> {

    default PageResult<MedicineReconciliationDetailDO> selectPage(MedicineReconciliationDetailPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MedicineReconciliationDetailDO>()
                .eqIfPresent(MedicineReconciliationDetailDO::getReconciliationId, reqVO.getReconciliationId())
                .likeIfPresent(MedicineReconciliationDetailDO::getMedicineName, reqVO.getMedicineName())
                .eqIfPresent(MedicineReconciliationDetailDO::getDosage, reqVO.getDosage())
                .eqIfPresent(MedicineReconciliationDetailDO::getUsageReason, reqVO.getUsageReason())
                .betweenIfPresent(MedicineReconciliationDetailDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(MedicineReconciliationDetailDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(MedicineReconciliationDetailDO::getComment, reqVO.getComment())
                .betweenIfPresent(MedicineReconciliationDetailDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MedicineReconciliationDetailDO::getId));
    }

    default List<MedicineReconciliationDetailDO> selectList(MedicineReconciliationDetailExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MedicineReconciliationDetailDO>()
                .eqIfPresent(MedicineReconciliationDetailDO::getReconciliationId, reqVO.getReconciliationId())
                .likeIfPresent(MedicineReconciliationDetailDO::getMedicineName, reqVO.getMedicineName())
                .eqIfPresent(MedicineReconciliationDetailDO::getDosage, reqVO.getDosage())
                .eqIfPresent(MedicineReconciliationDetailDO::getUsageReason, reqVO.getUsageReason())
                .betweenIfPresent(MedicineReconciliationDetailDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(MedicineReconciliationDetailDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(MedicineReconciliationDetailDO::getComment, reqVO.getComment())
                .betweenIfPresent(MedicineReconciliationDetailDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MedicineReconciliationDetailDO::getId));
    }

}
