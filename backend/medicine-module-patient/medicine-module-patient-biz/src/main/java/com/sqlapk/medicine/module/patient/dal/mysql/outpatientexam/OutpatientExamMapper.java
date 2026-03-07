package com.sqlapk.medicine.module.patient.dal.mysql.outpatientexam;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientexam.OutpatientExamDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 门诊药学检查项目明细 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface OutpatientExamMapper extends BaseMapperX<OutpatientExamDO> {

    default PageResult<OutpatientExamDO> selectPage(OutpatientExamPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OutpatientExamDO>()
                .eqIfPresent(OutpatientExamDO::getOutpatientId, reqVO.getOutpatientId())
                .eqIfPresent(OutpatientExamDO::getExaminationsId, reqVO.getExaminationsId())
                .betweenIfPresent(OutpatientExamDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OutpatientExamDO::getId));
    }

    default List<OutpatientExamDO> selectList(OutpatientExamExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<OutpatientExamDO>()
                .eqIfPresent(OutpatientExamDO::getOutpatientId, reqVO.getOutpatientId())
                .eqIfPresent(OutpatientExamDO::getExaminationsId, reqVO.getExaminationsId())
                .betweenIfPresent(OutpatientExamDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OutpatientExamDO::getId));
    }

}
