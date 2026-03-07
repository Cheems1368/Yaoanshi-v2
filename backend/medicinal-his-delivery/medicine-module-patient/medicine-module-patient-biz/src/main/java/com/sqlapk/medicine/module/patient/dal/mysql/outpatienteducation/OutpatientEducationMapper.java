package com.sqlapk.medicine.module.patient.dal.mysql.outpatienteducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatienteducation.OutpatientEducationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 门诊处方用药教育明细 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface OutpatientEducationMapper extends BaseMapperX<OutpatientEducationDO> {

    default PageResult<OutpatientEducationDO> selectPage(OutpatientEducationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OutpatientEducationDO>()
                .eqIfPresent(OutpatientEducationDO::getOutpatientId, reqVO.getOutpatientId())
                .eqIfPresent(OutpatientEducationDO::getEducationId, reqVO.getEducationId())
                .betweenIfPresent(OutpatientEducationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OutpatientEducationDO::getId));
    }

    default List<OutpatientEducationDO> selectList(OutpatientEducationExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<OutpatientEducationDO>()
                .eqIfPresent(OutpatientEducationDO::getOutpatientId, reqVO.getOutpatientId())
                .eqIfPresent(OutpatientEducationDO::getEducationId, reqVO.getEducationId())
                .betweenIfPresent(OutpatientEducationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OutpatientEducationDO::getId));
    }

}
