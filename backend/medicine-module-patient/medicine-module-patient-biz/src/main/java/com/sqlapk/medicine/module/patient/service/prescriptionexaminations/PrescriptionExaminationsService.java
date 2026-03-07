package com.sqlapk.medicine.module.patient.service.prescriptionexaminations;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptionexaminations.PrescriptionExaminationsDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 处方检查项目关系 Service 接口
 *
 * @author 管理员
 */
public interface PrescriptionExaminationsService {

    /**
     * 创建处方检查项目关系
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPrescriptionExaminations(@Valid PrescriptionExaminationsCreateReqVO createReqVO);

    /**
     * 更新处方检查项目关系
     *
     * @param updateReqVO 更新信息
     */
    void updatePrescriptionExaminations(@Valid PrescriptionExaminationsUpdateReqVO updateReqVO);

    /**
     * 删除处方检查项目关系
     *
     * @param id 编号
     */
    void deletePrescriptionExaminations(Long id);

    /**
     * 获得处方检查项目关系
     *
     * @param id 编号
     * @return 处方检查项目关系
     */
    PrescriptionExaminationsDO getPrescriptionExaminations(Long id);

    /**
     * 获得处方检查项目关系列表
     *
     * @param ids 编号
     * @return 处方检查项目关系列表
     */
    List<PrescriptionExaminationsDO> getPrescriptionExaminationsList(Collection<Long> ids);

    /**
     * 获得处方检查项目关系分页
     *
     * @param pageReqVO 分页查询
     * @return 处方检查项目关系分页
     */
    PageResult<PrescriptionExaminationsDO> getPrescriptionExaminationsPage(PrescriptionExaminationsPageReqVO pageReqVO);

    /**
     * 获得处方检查项目关系列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 处方检查项目关系列表
     */
    List<PrescriptionExaminationsDO> getPrescriptionExaminationsList(PrescriptionExaminationsExportReqVO exportReqVO);

}
