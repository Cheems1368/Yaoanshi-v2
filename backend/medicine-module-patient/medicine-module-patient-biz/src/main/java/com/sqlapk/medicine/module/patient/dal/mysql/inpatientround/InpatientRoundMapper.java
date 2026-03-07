package com.sqlapk.medicine.module.patient.dal.mysql.inpatientround;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientround.InpatientRoundDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 住院药学查房记录 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface InpatientRoundMapper extends BaseMapperX<InpatientRoundDO> {

    default PageResult<InpatientRoundDO> selectPage(InpatientRoundPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InpatientRoundDO>()
                .eqIfPresent(InpatientRoundDO::getInpatientId, reqVO.getInpatientId())
                .betweenIfPresent(InpatientRoundDO::getRoundDate, reqVO.getRoundDate())
                .eqIfPresent(InpatientRoundDO::getRoundContent, reqVO.getRoundContent())
                .betweenIfPresent(InpatientRoundDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InpatientRoundDO::getId));
    }

    default List<InpatientRoundDO> selectList(InpatientRoundExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<InpatientRoundDO>()
                .eqIfPresent(InpatientRoundDO::getInpatientId, reqVO.getInpatientId())
                .betweenIfPresent(InpatientRoundDO::getRoundDate, reqVO.getRoundDate())
                .eqIfPresent(InpatientRoundDO::getRoundContent, reqVO.getRoundContent())
                .betweenIfPresent(InpatientRoundDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InpatientRoundDO::getId));
    }

}
