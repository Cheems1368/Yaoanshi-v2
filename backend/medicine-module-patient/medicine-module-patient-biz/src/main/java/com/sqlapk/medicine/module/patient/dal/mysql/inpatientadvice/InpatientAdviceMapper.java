package com.sqlapk.medicine.module.patient.dal.mysql.inpatientadvice;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdvicePageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientadvice.InpatientAdviceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 住院药学不合理用药 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface InpatientAdviceMapper extends BaseMapperX<InpatientAdviceDO> {

    default PageResult<InpatientAdviceDO> selectPage(InpatientAdvicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InpatientAdviceDO>()
                .eqIfPresent(InpatientAdviceDO::getInpatientId, reqVO.getInpatientId())
                .betweenIfPresent(InpatientAdviceDO::getAdviceDate, reqVO.getAdviceDate())
                .eqIfPresent(InpatientAdviceDO::getAdviceContent, reqVO.getAdviceContent())
                .eqIfPresent(InpatientAdviceDO::getIrrationalReason, reqVO.getIrrationalReason())
                .eqIfPresent(InpatientAdviceDO::getMedicationSuggestion, reqVO.getMedicationSuggestion())
                .eqIfPresent(InpatientAdviceDO::getResult, reqVO.getResult())
                .betweenIfPresent(InpatientAdviceDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InpatientAdviceDO::getId));
    }

    default List<InpatientAdviceDO> selectList(InpatientAdviceExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<InpatientAdviceDO>()
                .eqIfPresent(InpatientAdviceDO::getInpatientId, reqVO.getInpatientId())
                .betweenIfPresent(InpatientAdviceDO::getAdviceDate, reqVO.getAdviceDate())
                .eqIfPresent(InpatientAdviceDO::getAdviceContent, reqVO.getAdviceContent())
                .eqIfPresent(InpatientAdviceDO::getIrrationalReason, reqVO.getIrrationalReason())
                .eqIfPresent(InpatientAdviceDO::getMedicationSuggestion, reqVO.getMedicationSuggestion())
                .eqIfPresent(InpatientAdviceDO::getResult, reqVO.getResult())
                .betweenIfPresent(InpatientAdviceDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InpatientAdviceDO::getId));
    }

}
