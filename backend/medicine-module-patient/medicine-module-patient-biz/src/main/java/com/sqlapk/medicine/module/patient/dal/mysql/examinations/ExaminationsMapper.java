package com.sqlapk.medicine.module.patient.dal.mysql.examinations;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.examinations.ExaminationsDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 检查项目 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface ExaminationsMapper extends BaseMapperX<ExaminationsDO> {

    default PageResult<ExaminationsDO> selectPage(ExaminationsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ExaminationsDO>()
                .likeIfPresent(ExaminationsDO::getName, reqVO.getName())
                .eqIfPresent(ExaminationsDO::getPrice, reqVO.getPrice())
                .eqIfPresent(ExaminationsDO::getDescription, reqVO.getDescription())
                .betweenIfPresent(ExaminationsDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ExaminationsDO::getId));
    }

    default List<ExaminationsDO> selectList(ExaminationsExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ExaminationsDO>()
                .likeIfPresent(ExaminationsDO::getName, reqVO.getName())
                .eqIfPresent(ExaminationsDO::getPrice, reqVO.getPrice())
                .eqIfPresent(ExaminationsDO::getDescription, reqVO.getDescription())
                .betweenIfPresent(ExaminationsDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ExaminationsDO::getId));
    }

}
