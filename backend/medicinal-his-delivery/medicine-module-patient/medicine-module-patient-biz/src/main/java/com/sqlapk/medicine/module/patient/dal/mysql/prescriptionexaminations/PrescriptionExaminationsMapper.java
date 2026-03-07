package com.sqlapk.medicine.module.patient.dal.mysql.prescriptionexaminations;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsPageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptionexaminations.PrescriptionExaminationsDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 处方检查项目关系 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface PrescriptionExaminationsMapper extends BaseMapperX<PrescriptionExaminationsDO> {

    default PageResult<PrescriptionExaminationsDO> selectPage(PrescriptionExaminationsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PrescriptionExaminationsDO>()
                .eqIfPresent(PrescriptionExaminationsDO::getPrescriptionId, reqVO.getPrescriptionId())
                .eqIfPresent(PrescriptionExaminationsDO::getExaminationId, reqVO.getExaminationId())
                .eqIfPresent(PrescriptionExaminationsDO::getResult, reqVO.getResult())
                .betweenIfPresent(PrescriptionExaminationsDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PrescriptionExaminationsDO::getId));
    }

    default List<PrescriptionExaminationsDO> selectList(PrescriptionExaminationsExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PrescriptionExaminationsDO>()
                .eqIfPresent(PrescriptionExaminationsDO::getPrescriptionId, reqVO.getPrescriptionId())
                .eqIfPresent(PrescriptionExaminationsDO::getExaminationId, reqVO.getExaminationId())
                .eqIfPresent(PrescriptionExaminationsDO::getResult, reqVO.getResult())
                .betweenIfPresent(PrescriptionExaminationsDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PrescriptionExaminationsDO::getId));
    }

}
