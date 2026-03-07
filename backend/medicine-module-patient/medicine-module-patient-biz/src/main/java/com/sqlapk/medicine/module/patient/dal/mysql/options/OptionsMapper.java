package com.sqlapk.medicine.module.patient.dal.mysql.options;

import java.util.*;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.module.patient.dal.dataobject.options.OptionsDO;
import org.apache.ibatis.annotations.Mapper;
import com.sqlapk.medicine.module.patient.controller.admin.options.vo.*;

/**
 * 问题选项 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface OptionsMapper extends BaseMapperX<OptionsDO> {

    default PageResult<OptionsDO> selectPage(OptionsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OptionsDO>()
                .eqIfPresent(OptionsDO::getContent, reqVO.getContent())
                .eqIfPresent(OptionsDO::getValue, reqVO.getValue())
                .eqIfPresent(OptionsDO::getScore, reqVO.getScore())
                .eqIfPresent(OptionsDO::getQuestionId, reqVO.getQuestionId())
                .betweenIfPresent(OptionsDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OptionsDO::getId));
    }

    default List<OptionsDO> selectList(OptionsExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<OptionsDO>()
                .eqIfPresent(OptionsDO::getContent, reqVO.getContent())
                .eqIfPresent(OptionsDO::getValue, reqVO.getValue())
                .eqIfPresent(OptionsDO::getScore, reqVO.getScore())
                .eqIfPresent(OptionsDO::getQuestionId, reqVO.getQuestionId())
                .betweenIfPresent(OptionsDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OptionsDO::getId));
    }

}
