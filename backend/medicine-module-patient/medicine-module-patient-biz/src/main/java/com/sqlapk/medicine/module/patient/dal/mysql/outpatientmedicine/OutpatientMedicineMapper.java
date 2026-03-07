package com.sqlapk.medicine.module.patient.dal.mysql.outpatientmedicine;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicinePageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientmedicine.OutpatientMedicineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 药学门诊 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface OutpatientMedicineMapper extends BaseMapperX<OutpatientMedicineDO> {

    default PageResult<OutpatientMedicineDO> selectPage(OutpatientMedicinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OutpatientMedicineDO>()
                .betweenIfPresent(OutpatientMedicineDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(OutpatientMedicineDO::getOutpatientId, reqVO.getOutpatientId())
                .eqIfPresent(OutpatientMedicineDO::getMeicineId, reqVO.getMeicineId())
                .orderByDesc(OutpatientMedicineDO::getId));
    }

    default List<OutpatientMedicineDO> selectList(OutpatientMedicineExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<OutpatientMedicineDO>()
                .betweenIfPresent(OutpatientMedicineDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(OutpatientMedicineDO::getOutpatientId, reqVO.getOutpatientId())
                .eqIfPresent(OutpatientMedicineDO::getMeicineId, reqVO.getMeicineId())
                .orderByDesc(OutpatientMedicineDO::getId));
    }

}
