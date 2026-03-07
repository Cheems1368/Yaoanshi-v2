package com.sqlapk.medicine.module.patient.dal.mysql.pharmacyoutpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.pharmacyoutpatient.PharmacyOutpatientDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 药学门诊 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface PharmacyOutpatientMapper extends BaseMapperX<PharmacyOutpatientDO> {

    default PageResult<PharmacyOutpatientDO> selectPage(PharmacyOutpatientPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PharmacyOutpatientDO>()
                .eqIfPresent(PharmacyOutpatientDO::getPrescriptionId, reqVO.getPrescriptionId())
                .likeIfPresent(PharmacyOutpatientDO::getMedicineName, reqVO.getMedicineName())
                .eqIfPresent(PharmacyOutpatientDO::getMedicineUsage, reqVO.getMedicineUsage())
                .betweenIfPresent(PharmacyOutpatientDO::getStartUsageTime, reqVO.getStartUsageTime())
                .betweenIfPresent(PharmacyOutpatientDO::getStopUsageTime, reqVO.getStopUsageTime())
                .eqIfPresent(PharmacyOutpatientDO::getStopReason, reqVO.getStopReason())
                .eqIfPresent(PharmacyOutpatientDO::getMrpType, reqVO.getMrpType())
                .eqIfPresent(PharmacyOutpatientDO::getWeight, reqVO.getWeight())
                .eqIfPresent(PharmacyOutpatientDO::getMrpDesc, reqVO.getMrpDesc())
                .betweenIfPresent(PharmacyOutpatientDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PharmacyOutpatientDO::getId));
    }

    default List<PharmacyOutpatientDO> selectList(PharmacyOutpatientExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PharmacyOutpatientDO>()
                .eqIfPresent(PharmacyOutpatientDO::getPrescriptionId, reqVO.getPrescriptionId())
                .likeIfPresent(PharmacyOutpatientDO::getMedicineName, reqVO.getMedicineName())
                .eqIfPresent(PharmacyOutpatientDO::getMedicineUsage, reqVO.getMedicineUsage())
                .betweenIfPresent(PharmacyOutpatientDO::getStartUsageTime, reqVO.getStartUsageTime())
                .betweenIfPresent(PharmacyOutpatientDO::getStopUsageTime, reqVO.getStopUsageTime())
                .eqIfPresent(PharmacyOutpatientDO::getStopReason, reqVO.getStopReason())
                .eqIfPresent(PharmacyOutpatientDO::getMrpType, reqVO.getMrpType())
                .eqIfPresent(PharmacyOutpatientDO::getWeight, reqVO.getWeight())
                .eqIfPresent(PharmacyOutpatientDO::getMrpDesc, reqVO.getMrpDesc())
                .betweenIfPresent(PharmacyOutpatientDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PharmacyOutpatientDO::getId));
    }

}
