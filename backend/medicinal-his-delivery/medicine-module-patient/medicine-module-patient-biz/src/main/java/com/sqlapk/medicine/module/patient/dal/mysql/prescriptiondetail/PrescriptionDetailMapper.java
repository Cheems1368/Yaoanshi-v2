package com.sqlapk.medicine.module.patient.dal.mysql.prescriptiondetail;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptiondetail.PrescriptionDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 患者处方 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface PrescriptionDetailMapper extends BaseMapperX<PrescriptionDetailDO> {

    default PageResult<PrescriptionDetailDO> selectPage(PrescriptionDetailPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PrescriptionDetailDO>()
                .eqIfPresent(PrescriptionDetailDO::getPrescriptionId, reqVO.getPrescriptionId())
                .eqIfPresent(PrescriptionDetailDO::getMedicineId, reqVO.getMedicineId())
                .eqIfPresent(PrescriptionDetailDO::getDosage, reqVO.getDosage())
                .eqIfPresent(PrescriptionDetailDO::getUsageInfo, reqVO.getUsageInfo())
                .eqIfPresent(PrescriptionDetailDO::getFrequency, reqVO.getFrequency())
                .eqIfPresent(PrescriptionDetailDO::getQuality, reqVO.getQuality())
                .eqIfPresent(PrescriptionDetailDO::getUnit, reqVO.getUnit())
                .betweenIfPresent(PrescriptionDetailDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PrescriptionDetailDO::getId));
    }

    default List<PrescriptionDetailDO> selectList(PrescriptionDetailExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PrescriptionDetailDO>()
                .eqIfPresent(PrescriptionDetailDO::getPrescriptionId, reqVO.getPrescriptionId())
                .eqIfPresent(PrescriptionDetailDO::getMedicineId, reqVO.getMedicineId())
                .eqIfPresent(PrescriptionDetailDO::getDosage, reqVO.getDosage())
                .eqIfPresent(PrescriptionDetailDO::getUsageInfo, reqVO.getUsageInfo())
                .eqIfPresent(PrescriptionDetailDO::getFrequency, reqVO.getFrequency())
                .eqIfPresent(PrescriptionDetailDO::getQuality, reqVO.getQuality())
                .eqIfPresent(PrescriptionDetailDO::getUnit, reqVO.getUnit())
                .betweenIfPresent(PrescriptionDetailDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PrescriptionDetailDO::getId));
    }

}
